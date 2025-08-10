// @ts-nocheck
import { useState, useEffect } from 'react';
import { useToast } from '@/hooks/use-toast';
import { DataStructure, Field } from '@/types/dataStructures';
import { parseJsonStructure, parseWsdlStructure, buildNestedStructure } from '@/utils/structureParsers';
import { structureService, DataStructureCreate } from '@/services/structureService';

const sampleStructures: DataStructure[] = [
  {
    id: '1',
    name: 'Business Component Order',
    type: 'json',
    description: 'Standard businessComponent order structure',
    structure: {
      orderId: 'string',
      businessComponentId: 'string',
      items: 'array',
      totalAmount: 'decimal',
      orderDate: 'datetime'
    },
    createdAt: '2024-01-15',
    usage: 'source'
  },
  {
    id: '2',
    name: 'Payment Response',
    type: 'soap',
    description: 'Payment gateway response format',
    structure: {
      transactionId: 'string',
      status: 'string',
      amount: 'decimal',
      currency: 'string'
    },
    createdAt: '2024-01-10',
    usage: 'target'
  },
  {
    id: '3',
    name: 'Business Component Profile',
    type: 'json',
    description: 'Detailed businessComponent profile with nested address information',
    structure: {
      businessComponent: {
        id: 'string',
        firstName: 'string',
        lastName: 'string',
        email: 'string',
        phone: 'string',
        dateOfBirth: 'date',
        address: {
          street: 'string',
          city: 'string',
          state: 'string',
          zipCode: 'string',
          country: 'string'
        },
        preferences: {
          newsletter: 'boolean',
          smsNotifications: 'boolean',
          language: 'string'
        }
      }
    },
    createdAt: '2024-01-12',
    usage: 'source'
  },
  {
    id: '4',
    name: 'Product Catalog',
    type: 'json',
    description: 'Product information with categories and pricing',
    structure: {
      products: {
        id: 'string',
        name: 'string',
        description: 'string',
        category: {
          id: 'string',
          name: 'string',
          parentCategory: 'string'
        },
        pricing: {
          basePrice: 'decimal',
          currency: 'string',
          discountPercentage: 'decimal',
          taxRate: 'decimal'
        },
        inventory: {
          quantity: 'integer',
          warehouse: 'string',
          lastUpdated: 'datetime'
        },
        specifications: {
          weight: 'decimal',
          dimensions: {
            length: 'decimal',
            width: 'decimal',
            height: 'decimal'
          },
          color: 'string',
          material: 'string'
        }
      }
    },
    createdAt: '2024-01-08',
    usage: 'source'
  },
  {
    id: '5',
    name: 'Invoice Data',
    type: 'xsd',
    description: 'Standard invoice format for accounting systems',
    structure: {
      invoice: {
        header: {
          invoiceNumber: 'string',
          invoiceDate: 'date',
          dueDate: 'date',
          currency: 'string'
        },
        vendor: {
          vendorId: 'string',
          companyName: 'string',
          address: {
            street: 'string',
            city: 'string',
            postalCode: 'string',
            country: 'string'
          },
          taxId: 'string'
        },
        businessComponent: {
          businessComponentId: 'string',
          companyName: 'string',
          contactPerson: 'string',
          address: {
            street: 'string',
            city: 'string',
            postalCode: 'string',
            country: 'string'
          }
        },
        lineItems: {
          item: {
            lineNumber: 'integer',
            productId: 'string',
            description: 'string',
            quantity: 'decimal',
            unitPrice: 'decimal',
            taxAmount: 'decimal',
            lineTotal: 'decimal'
          }
        },
        totals: {
          subtotal: 'decimal',
          totalTax: 'decimal',
          totalAmount: 'decimal'
        }
      }
    },
    createdAt: '2024-01-05',
    usage: 'target'
  },
  {
    id: '6',
    name: 'Sales Report',
    type: 'json',
    description: 'Comprehensive sales reporting structure',
    structure: {
      report: {
        metadata: {
          reportId: 'string',
          generatedAt: 'datetime',
          period: {
            startDate: 'date',
            endDate: 'date'
          },
          reportType: 'string'
        },
        summary: {
          totalSales: 'decimal',
          totalOrders: 'integer',
          averageOrderValue: 'decimal',
          topProducts: {
            productId: 'string',
            productName: 'string',
            salesCount: 'integer',
            revenue: 'decimal'
          }
        },
        salesData: {
          daily: {
            date: 'date',
            orders: 'integer',
            revenue: 'decimal',
            customers: 'integer'
          },
          byRegion: {
            region: 'string',
            orders: 'integer',
            revenue: 'decimal',
            growthRate: 'decimal'
          }
        }
      }
    },
    createdAt: '2024-01-03',
    usage: 'target'
  },
  {
    id: '7',
    name: 'Employee Record',
    type: 'custom',
    description: 'HR system employee data structure',
    structure: {
      employee: {
        personalInfo: {
          employeeId: 'string',
          firstName: 'string',
          lastName: 'string',
          middleName: 'string',
          dateOfBirth: 'date',
          ssn: 'string',
          address: {
            home: {
              street: 'string',
              city: 'string',
              state: 'string',
              zipCode: 'string'
            },
            mailing: {
              street: 'string',
              city: 'string',
              state: 'string',
              zipCode: 'string'
            }
          }
        },
        employment: {
          hireDate: 'date',
          department: 'string',
          position: 'string',
          manager: 'string',
          salary: {
            amount: 'decimal',
            currency: 'string',
            payFrequency: 'string'
          },
          benefits: {
            healthInsurance: 'boolean',
            dentalInsurance: 'boolean',
            retirement401k: 'boolean',
            vacationDays: 'integer'
          }
        }
      }
    },
    createdAt: '2024-01-01',
    usage: 'source'
  }
];

export const useDataStructures = () => {
  const [structures, setStructures] = useState<DataStructure[]>([]);
  const [selectedStructure, setSelectedStructure] = useState<DataStructure | null>(null);
  const [loading, setLoading] = useState(false);
  const { toast } = useToast();

  // Load structures from backend on component mount
  useEffect(() => {
    loadStructures();
  }, []);

  const loadStructures = async () => {
    try {
      setLoading(true);
      console.log('Loading data structures from backend...');
      const response = await structureService.getStructures();
      
      if (response.success && response.data) {
        console.log('API structures loaded:', response.data.structures || response.data);
        const structures = response.data.structures || response.data || [];
        
        // Check if response is actually HTML (endpoint doesn't exist)
        if (typeof structures === 'string' && structures.includes('<!DOCTYPE html>')) {
          console.log('API endpoint not implemented yet, showing sample data');
          setStructures(sampleStructures);
          toast({
            title: "Info",
            description: "Data structures API not available yet. Showing sample data.",
          });
          return;
        }
        
        // Set structures from API, even if empty
        console.log(`API returned ${structures.length} structures`);
        setStructures(structures);
      } else {
        console.log('API failed, showing empty list');
        setStructures([]);
      }
    } catch (error) {
      console.error('Error loading structures, showing empty list:', error);
      setStructures([]);
    } finally {
      setLoading(false);
    }
  };

  const updateStructure = async (
    id: string,
    structureName: string,
    structureDescription: string,
    structureUsage: 'source' | 'target',
    jsonInput: string,
    xsdInput: string,
    edmxInput: string,
    wsdlInput: string,
    customFields: Field[],
    selectedStructureType: string,
    namespaceConfig: any,
    businessComponentId?: string
  ) => {
    if (!structureName) {
      toast({
        title: "Validation Error",
        description: "Please provide a structure name",
        variant: "destructive",
      });
      return false;
    }

    let structure: any = {};
    let metadata: any = {};
    
    if (jsonInput) {
      structure = parseJsonStructure(jsonInput);
    } else if (wsdlInput) {
      const wsdlResult = parseWsdlStructure(wsdlInput);
      if (wsdlResult && typeof wsdlResult === 'object' && 'structure' in wsdlResult) {
        structure = wsdlResult.structure;
        if (wsdlResult.operationInfo) {
          metadata.operationInfo = wsdlResult.operationInfo;
        }
      } else {
        structure = wsdlResult;
      }
    } else if (xsdInput) {
      structure = { message: 'XSD parsing not fully implemented yet' };
    } else if (edmxInput) {
      structure = { message: 'EDMX parsing not fully implemented yet' };
    } else if (customFields.length > 0) {
      structure = buildNestedStructure(customFields);
    } else {
      toast({
        title: "Validation Error",
        description: "Please define a structure using JSON, XSD, EDMX, WSDL, or custom fields",
        variant: "destructive",
      });
      return false;
    }

    // Determine original content and format for updates
    let originalContent: string | undefined;
    let originalFormat: string | undefined;
    
    if (jsonInput) {
      originalContent = jsonInput;
      originalFormat = 'json';
    } else if (wsdlInput) {
      originalContent = wsdlInput;
      originalFormat = 'xml';
    } else if (xsdInput) {
      originalContent = xsdInput;
      originalFormat = 'xml';
    } else if (edmxInput) {
      originalContent = edmxInput;
      originalFormat = 'xml';
    }
    
    const updates: Partial<DataStructureCreate> = {
      name: structureName,
      type: jsonInput ? 'json' : wsdlInput ? 'wsdl' : xsdInput ? 'xsd' : 'custom',
      description: structureDescription,
      usage: structureUsage as 'source' | 'target' | 'both',
      structure,
      originalContent,
      originalFormat,
      businessComponentId,
      namespace: (selectedStructureType === 'xsd' || selectedStructureType === 'wsdl' || selectedStructureType === 'edmx') && namespaceConfig.uri ? namespaceConfig : undefined,
      metadata: Object.keys(metadata).length > 0 ? metadata : undefined
    };

    try {
      console.log('Updating structure:', id, updates);
      const response = await structureService.updateStructure(id, updates);
      
      if (response.success && response.data) {
        console.log('Structure updated successfully:', response.data);
        // Reload structures to get the updated list
        await loadStructures();
        
        toast({
          title: "Structure Updated",
          description: `Data structure "${structureName}" has been updated successfully`,
        });
        return true;
      } else {
        console.error('Failed to update structure:', response.error);
        toast({
          title: "Update Failed",
          description: "Failed to update data structure",
          variant: "destructive",
        });
        return false;
      }
    } catch (error: any) {
      console.error('Error updating structure:', error);
      
      // Extract the actual error message
      let errorMessage = "An error occurred while updating the data structure";
      if (error.response) {
        console.error('Error response data:', error.response);
        // Handle Spring Boot problem details response
        if (error.response.detail) {
          errorMessage = error.response.detail;
        } else if (error.response.message) {
          errorMessage = error.response.message;
        } else if (error.response.error) {
          errorMessage = error.response.error;
        } else if (typeof error.response === 'string') {
          errorMessage = error.response;
        }
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      toast({
        title: "Update Error",
        description: errorMessage,
        variant: "destructive",
      });
      return false;
    }
  };

  const saveStructure = async (
    structureName: string,
    structureDescription: string,
    structureUsage: 'source' | 'target',
    jsonInput: string,
    xsdInput: string,
    edmxInput: string,
    wsdlInput: string,
    customFields: Field[],
    selectedStructureType: string,
    namespaceConfig: any,
    businessComponentId?: string
  ) => {
    if (!structureName) {
      toast({
        title: "Validation Error",
        description: "Please provide a structure name",
        variant: "destructive",
      });
      return false;
    }

    let structure: any = {};
    let metadata: any = {};
    
    if (jsonInput) {
      structure = parseJsonStructure(jsonInput);
    } else if (wsdlInput) {
      const wsdlResult = parseWsdlStructure(wsdlInput);
      if (wsdlResult && typeof wsdlResult === 'object' && 'structure' in wsdlResult) {
        structure = wsdlResult.structure;
        if (wsdlResult.operationInfo) {
          metadata.operationInfo = wsdlResult.operationInfo;
        }
      } else {
        structure = wsdlResult;
      }
    } else if (xsdInput) {
      structure = { message: 'XSD parsing not fully implemented yet' };
    } else if (edmxInput) {
      structure = { message: 'EDMX parsing not fully implemented yet' };
    } else if (customFields.length > 0) {
      structure = buildNestedStructure(customFields);
    } else {
      toast({
        title: "Validation Error",
        description: "Please define a structure using JSON, XSD, EDMX, WSDL, or custom fields",
        variant: "destructive",
      });
      return false;
    }

    // Determine original content and format
    let originalContent: string | undefined;
    let originalFormat: string | undefined;
    
    if (jsonInput) {
      originalContent = jsonInput;
      originalFormat = 'json';
    } else if (wsdlInput) {
      originalContent = wsdlInput;
      originalFormat = 'xml';
    } else if (xsdInput) {
      originalContent = xsdInput;
      originalFormat = 'xml';
    } else if (edmxInput) {
      originalContent = edmxInput;
      originalFormat = 'xml';
    }
    
    const structureData: DataStructureCreate = {
      name: structureName,
      type: jsonInput ? 'json' : wsdlInput ? 'wsdl' : xsdInput ? 'xsd' : 'custom',
      description: structureDescription,
      usage: structureUsage as 'source' | 'target' | 'both',
      structure,
      originalContent,
      originalFormat,
      businessComponentId,
      namespace: (selectedStructureType === 'xsd' || selectedStructureType === 'wsdl' || selectedStructureType === 'edmx') && namespaceConfig.uri ? namespaceConfig : undefined,
      metadata: Object.keys(metadata).length > 0 ? metadata : undefined
    };

    try {
      console.log('Saving structure to backend:', structureData);
      const response = await structureService.createStructure(structureData);
      
      if (response.success && response.data) {
        console.log('Structure saved successfully:', response.data);
        // Reload structures to get the updated list
        await loadStructures();
        
        toast({
          title: "Structure Saved",
          description: `Data structure "${structureName}" has been created successfully`,
        });
        return true;
      } else {
        console.error('Failed to save structure:', response.error);
        toast({
          title: "Save Failed",
          description: "Failed to save data structure to the backend",
          variant: "destructive",
        });
        return false;
      }
    } catch (error: any) {
      console.error('Error saving structure:', error);
      
      // Extract the actual error message
      let errorMessage = "An error occurred while saving the data structure";
      if (error.response) {
        console.error('Error response data:', error.response);
        // Handle Spring Boot problem details response
        if (error.response.detail) {
          errorMessage = error.response.detail;
        } else if (error.response.message) {
          errorMessage = error.response.message;
        } else if (error.response.error) {
          errorMessage = error.response.error;
        } else if (typeof error.response === 'string') {
          errorMessage = error.response;
        }
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      toast({
        title: "Save Error",
        description: errorMessage,
        variant: "destructive",
      });
      return false;
    }
  };

  const deleteStructure = async (id: string) => {
    try {
      console.log('Deleting structure:', id);
      const response = await structureService.deleteStructure(id);
      
      if (response.success) {
        console.log('Structure deleted successfully');
        // Reload structures to get the updated list
        await loadStructures();
        
        if (selectedStructure?.id === id) {
          setSelectedStructure(null);
        }
        
        toast({
          title: "Structure Deleted",
          description: "Data structure has been removed",
        });
      } else {
        console.error('Failed to delete structure:', response.error);
        toast({
          title: "Delete Failed",
          description: "Failed to delete data structure",
          variant: "destructive",
        });
      }
    } catch (error) {
      console.error('Error deleting structure:', error);
      toast({
        title: "Delete Error",
        description: "An error occurred while deleting the data structure",
        variant: "destructive",
      });
    }
  };

  const duplicateStructure = async (structure: DataStructure) => {
    try {
      console.log('Duplicating structure:', structure.name);
      const response = await structureService.cloneStructure(structure.id, `${structure.name} (Copy)`);
      
      if (response.success && response.data) {
        console.log('Structure duplicated successfully:', response.data);
        // Reload structures to get the updated list
        await loadStructures();
        
        toast({
          title: "Structure Duplicated",
          description: `Created copy of "${structure.name}"`,
        });
      } else {
        console.error('Failed to duplicate structure:', response.error);
        toast({
          title: "Duplicate Failed",
          description: "Failed to duplicate data structure",
          variant: "destructive",
        });
      }
    } catch (error) {
      console.error('Error duplicating structure:', error);
      toast({
        title: "Duplicate Error",
        description: "An error occurred while duplicating the data structure",
        variant: "destructive",
      });
    }
  };

  return {
    structures,
    selectedStructure,
    setSelectedStructure,
    saveStructure,
    updateStructure,
    deleteStructure,
    duplicateStructure,
    loading,
    refreshStructures: loadStructures
  };
};