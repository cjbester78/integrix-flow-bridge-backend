import { FlowDefinition, FlowExecution, StepExecution, ExecutionError, ExecutionLog } from '@/types/flow';
import { toast } from '@/hooks/use-toast';

export interface ExecutionContext {
  flowId: string;
  executionId: string;
  variables: Record<string, any>;
  stepResults: Record<string, any>;
  currentStep?: string;
  metadata: Record<string, any>;
}

export interface StepProcessor {
  process(step: any, context: ExecutionContext): Promise<any>;
  validateInput(step: any, input: any): boolean;
  cleanup?(context: ExecutionContext): Promise<void>;
}

class FlowExecutionEngine {
  private stepProcessors: Map<string, StepProcessor> = new Map();
  private activeExecutions: Map<string, FlowExecution> = new Map();
  private executionListeners: Map<string, ((execution: FlowExecution) => void)[]> = new Map();

  constructor() {
    this.initializeStepProcessors();
  }

  // Register step processors for different step types
  private initializeStepProcessors() {
    this.stepProcessors.set('adapter', new AdapterStepProcessor());
    this.stepProcessors.set('transformation', new TransformationStepProcessor());
    this.stepProcessors.set('condition', new ConditionStepProcessor());
    this.stepProcessors.set('loop', new LoopStepProcessor());
    this.stepProcessors.set('delay', new DelayStepProcessor());
  }

  // Execute a flow
  async executeFlow(
    flowDefinition: FlowDefinition, 
    triggerType: string = 'manual',
    triggerContext: Record<string, any> = {},
    userId: string = 'system'
  ): Promise<FlowExecution> {
    const executionId = `exec_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    const execution: FlowExecution = {
      id: executionId,
      flowId: flowDefinition.id,
      flowVersion: flowDefinition.version,
      status: 'pending',
      startTime: new Date().toISOString(),
      triggeredBy: userId,
      triggerType,
      context: triggerContext,
      steps: flowDefinition.steps.map(step => ({
        stepId: step.id,
        stepName: step.name,
        status: 'pending',
        retryCount: 0,
        logs: []
      })),
      metrics: {
        totalSteps: flowDefinition.steps.length,
        completedSteps: 0,
        failedSteps: 0,
        skippedSteps: 0,
        dataProcessed: 0
      }
    };

    this.activeExecutions.set(executionId, execution);
    this.notifyListeners(executionId, execution);

    try {
      await this.runExecution(flowDefinition, execution);
    } catch (error) {
      execution.status = 'failed';
      execution.error = {
        code: 'EXECUTION_FAILED',
        message: error instanceof Error ? error.message : 'Unknown error',
        timestamp: new Date().toISOString()
      };
      
      toast({
        title: "Flow Execution Failed",
        description: `Flow "${flowDefinition.name}" failed: ${execution.error.message}`,
        variant: "destructive"
      });
    } finally {
      execution.endTime = new Date().toISOString();
      execution.duration = new Date(execution.endTime).getTime() - new Date(execution.startTime).getTime();
      this.activeExecutions.delete(executionId);
      this.notifyListeners(executionId, execution);
    }

    return execution;
  }

  // Core execution logic
  private async runExecution(flowDefinition: FlowDefinition, execution: FlowExecution): Promise<void> {
    execution.status = 'running';
    this.notifyListeners(execution.id, execution);

    const context: ExecutionContext = {
      flowId: flowDefinition.id,
      executionId: execution.id,
      variables: { ...flowDefinition.variables, ...execution.context },
      stepResults: {},
      metadata: {}
    };

    for (let i = 0; i < flowDefinition.steps.length; i++) {
      const step = flowDefinition.steps[i];
      const stepExecution = execution.steps[i];
      
      context.currentStep = step.id;
      
      try {
        await this.executeStep(step, stepExecution, context, flowDefinition.settings);
        execution.metrics.completedSteps++;
      } catch (error) {
        execution.metrics.failedSteps++;
        
        if (flowDefinition.settings.errorHandling === 'stop') {
          throw error;
        } else if (flowDefinition.settings.errorHandling === 'retry') {
          // Retry logic would go here
          throw error;
        }
        // Continue with next step if errorHandling is 'continue'
      }

      this.notifyListeners(execution.id, execution);
    }

    execution.status = execution.metrics.failedSteps > 0 ? 'failed' : 'completed';
    
    toast({
      title: "Flow Execution Complete",
      description: `Flow "${flowDefinition.name}" executed successfully`,
      variant: execution.status === 'completed' ? "default" : "destructive"
    });
  }

  // Execute a single step
  private async executeStep(
    step: any,
    stepExecution: StepExecution,
    context: ExecutionContext,
    settings: any
  ): Promise<void> {
    stepExecution.status = 'running';
    stepExecution.startTime = new Date().toISOString();
    
    this.addLog(stepExecution, 'info', `Starting step: ${step.name}`);

    const processor = this.stepProcessors.get(step.type);
    if (!processor) {
      throw new Error(`No processor found for step type: ${step.type}`);
    }

    try {
      // Apply timeout if specified
      const timeoutPromise = new Promise((_, reject) => {
        setTimeout(() => reject(new Error('Step execution timeout')), settings.timeout || 30000);
      });

      const executionPromise = processor.process(step, context);
      stepExecution.output = await Promise.race([executionPromise, timeoutPromise]);
      
      context.stepResults[step.id] = stepExecution.output;
      stepExecution.status = 'completed';
      
      this.addLog(stepExecution, 'info', `Step completed successfully`);
      
    } catch (error) {
      stepExecution.status = 'failed';
      stepExecution.error = {
        code: 'STEP_EXECUTION_FAILED',
        message: error instanceof Error ? error.message : 'Unknown error',
        timestamp: new Date().toISOString()
      };
      
      this.addLog(stepExecution, 'error', `Step failed: ${stepExecution.error.message}`);
      throw error;
    } finally {
      stepExecution.endTime = new Date().toISOString();
      stepExecution.duration = new Date(stepExecution.endTime).getTime() - new Date(stepExecution.startTime).getTime();
    }
  }

  // Add log entry to step execution
  private addLog(stepExecution: StepExecution, level: 'debug' | 'info' | 'warn' | 'error', message: string, data?: any) {
    stepExecution.logs.push({
      timestamp: new Date().toISOString(),
      level,
      message,
      data
    });
  }

  // Pause flow execution
  async pauseExecution(executionId: string): Promise<void> {
    const execution = this.activeExecutions.get(executionId);
    if (execution && execution.status === 'running') {
      execution.status = 'paused';
      this.notifyListeners(executionId, execution);
    }
  }

  // Resume flow execution
  async resumeExecution(executionId: string): Promise<void> {
    const execution = this.activeExecutions.get(executionId);
    if (execution && execution.status === 'paused') {
      execution.status = 'running';
      this.notifyListeners(executionId, execution);
    }
  }

  // Cancel flow execution
  async cancelExecution(executionId: string): Promise<void> {
    const execution = this.activeExecutions.get(executionId);
    if (execution && ['running', 'paused'].includes(execution.status)) {
      execution.status = 'cancelled';
      execution.endTime = new Date().toISOString();
      this.activeExecutions.delete(executionId);
      this.notifyListeners(executionId, execution);
    }
  }

  // Get active executions
  getActiveExecutions(): FlowExecution[] {
    return Array.from(this.activeExecutions.values());
  }

  // Subscribe to execution updates
  subscribeToExecution(executionId: string, callback: (execution: FlowExecution) => void): void {
    if (!this.executionListeners.has(executionId)) {
      this.executionListeners.set(executionId, []);
    }
    this.executionListeners.get(executionId)!.push(callback);
  }

  // Unsubscribe from execution updates
  unsubscribeFromExecution(executionId: string, callback: (execution: FlowExecution) => void): void {
    const listeners = this.executionListeners.get(executionId);
    if (listeners) {
      const index = listeners.indexOf(callback);
      if (index > -1) {
        listeners.splice(index, 1);
      }
    }
  }

  // Notify listeners of execution updates
  private notifyListeners(executionId: string, execution: FlowExecution): void {
    const listeners = this.executionListeners.get(executionId);
    if (listeners) {
      listeners.forEach(callback => callback(execution));
    }
  }
}

// Step Processors Implementation
class AdapterStepProcessor implements StepProcessor {
  async process(step: any, context: ExecutionContext): Promise<any> {
    // Simulate adapter execution
    await new Promise(resolve => setTimeout(resolve, 1000));
    return { status: 'success', data: `Adapter ${step.name} executed` };
  }

  validateInput(step: any, input: any): boolean {
    return true;
  }
}

class TransformationStepProcessor implements StepProcessor {
  async process(step: any, context: ExecutionContext): Promise<any> {
    // Simulate transformation execution
    await new Promise(resolve => setTimeout(resolve, 800));
    return { status: 'success', transformedData: `Transformed by ${step.name}` };
  }

  validateInput(step: any, input: any): boolean {
    return true;
  }
}

class ConditionStepProcessor implements StepProcessor {
  async process(step: any, context: ExecutionContext): Promise<any> {
    // Simulate condition evaluation
    await new Promise(resolve => setTimeout(resolve, 200));
    return { status: 'success', conditionResult: true };
  }

  validateInput(step: any, input: any): boolean {
    return true;
  }
}

class LoopStepProcessor implements StepProcessor {
  async process(step: any, context: ExecutionContext): Promise<any> {
    // Simulate loop execution
    await new Promise(resolve => setTimeout(resolve, 1500));
    return { status: 'success', iterations: 3 };
  }

  validateInput(step: any, input: any): boolean {
    return true;
  }
}

class DelayStepProcessor implements StepProcessor {
  async process(step: any, context: ExecutionContext): Promise<any> {
    const delay = step.configuration?.delay || 1000;
    await new Promise(resolve => setTimeout(resolve, delay));
    return { status: 'success', delayedFor: delay };
  }

  validateInput(step: any, input: any): boolean {
    return true;
  }
}

export const flowExecutionEngine = new FlowExecutionEngine();