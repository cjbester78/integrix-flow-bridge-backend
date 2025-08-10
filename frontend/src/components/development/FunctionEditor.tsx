// @ts-nocheck - Temporary suppression for unused imports/variables
import React from 'react';
import Editor from '@monaco-editor/react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Loader2, Play, Save, Code, AlertCircle, CheckCircle2 } from 'lucide-react';

interface FunctionEditorProps {
  functionName: string;
  code: string;
  language: 'JAVA' | 'JAVASCRIPT' | 'GROOVY' | 'PYTHON';
  onChange: (value: string) => void;
  onSave?: () => void;
  onCompile?: () => void;
  onTest?: () => void;
  readOnly?: boolean;
  errors?: string[];
  warnings?: string[];
  isCompiling?: boolean;
  isSaving?: boolean;
}

export function FunctionEditor({
  functionName,
  code,
  language,
  onChange,
  onSave,
  onCompile,
  onTest,
  readOnly = false,
  errors = [],
  warnings = [],
  isCompiling = false,
  isSaving = false
}: FunctionEditorProps) {
  const getMonacoLanguage = (lang: string): string => {
    switch (lang) {
      case 'JAVA':
        return 'java';
      case 'JAVASCRIPT':
        return 'javascript';
      case 'GROOVY':
        return 'java'; // Groovy syntax is similar to Java
      case 'PYTHON':
        return 'python';
      default:
        return 'plaintext';
    }
  };

  return (
    <Card className="h-full flex flex-col">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <Code className="h-5 w-5" />
            {functionName}
          </CardTitle>
          <div className="flex gap-2">
            {language === 'JAVA' && onCompile && (
              <Button
                variant="outline"
                size="sm"
                onClick={onCompile}
                disabled={isCompiling || readOnly}
              >
                {isCompiling ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Compiling...
                  </>
                ) : (
                  <>
                    <Code className="h-4 w-4 mr-2" />
                    Compile
                  </>
                )}
              </Button>
            )}
            {onTest && (
              <Button
                variant="outline"
                size="sm"
                onClick={onTest}
                disabled={readOnly || errors.length > 0}
              >
                <Play className="h-4 w-4 mr-2" />
                Test
              </Button>
            )}
            {onSave && (
              <Button
                size="sm"
                onClick={onSave}
                disabled={isSaving || readOnly || errors.length > 0}
              >
                {isSaving ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Saving...
                  </>
                ) : (
                  <>
                    <Save className="h-4 w-4 mr-2" />
                    Save
                  </>
                )}
              </Button>
            )}
          </div>
        </div>
      </CardHeader>
      <CardContent className="flex-1 flex flex-col p-0">
        {errors.length > 0 && (
          <Alert variant="destructive" className="mx-6 mb-3">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              <div className="font-semibold mb-1">Compilation Errors:</div>
              <ul className="list-disc list-inside text-sm">
                {errors.map((error, index) => (
                  <li key={index}>{error}</li>
                ))}
              </ul>
            </AlertDescription>
          </Alert>
        )}
        
        {warnings.length > 0 && (
          <Alert className="mx-6 mb-3">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              <div className="font-semibold mb-1">Warnings:</div>
              <ul className="list-disc list-inside text-sm">
                {warnings.map((warning, index) => (
                  <li key={index}>{warning}</li>
                ))}
              </ul>
            </AlertDescription>
          </Alert>
        )}

        {errors.length === 0 && warnings.length === 0 && isCompiling === false && language === 'JAVA' && (
          <Alert className="mx-6 mb-3 border-green-200 bg-green-50 text-success">
            <CheckCircle2 className="h-4 w-4" />
            <AlertDescription>
              Code compiled successfully!
            </AlertDescription>
          </Alert>
        )}
        
        <div className="flex-1 border-t">
          <Editor
            height="100%"
            language={getMonacoLanguage(language)}
            value={code}
            onChange={(value) => onChange(value || '')}
            options={{
              readOnly,
              minimap: { enabled: false },
              fontSize: 14,
              lineNumbers: 'on',
              renderLineHighlight: 'all',
              scrollBeyondLastLine: false,
              automaticLayout: true,
              tabSize: 4,
              wordWrap: 'on',
              theme: 'vs-light'
            }}
          />
        </div>
      </CardContent>
    </Card>
  );
}