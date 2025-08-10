import { useState } from 'react';
import { useToast } from '@/hooks/use-toast';
import { DataStructure, Field } from '@/types/dataStructures';
import { parseJsonStructure, parseWsdlStructure, buildNestedStructure } from '@/utils/structureParsers';

const sampleStructures: DataStructure[] = [
  {
    id: '1',
    name: 'Customer Order',
    type: 'json',
    description: 'Standard customer order structure',
    structure: {
      orderId: 'string',
      customerId: 'string',
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
    name: 'Customer Profile',
    type: 'json',
    description: 'Detailed customer profile with nested address information',
    structure: {
      customer: {
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
        customer: {
          customerId: 'string',
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
  const [structures, setStructures] = useState<DataStructure[]>(sampleStructures);
  const [selectedStructure, setSelectedStructure] = useState<DataStructure | null>(null);
  const { toast } = useToast();

  const saveStructure = (
    structureName: string,
    structureDescription: string,
    structureUsage: 'source' | 'target',
    jsonInput: string,
    xsdInput: string,
    edmxInput: string,
    wsdlInput: string,
    customFields: Field[],
    selectedStructureType: string,
    namespaceConfig: any
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
    
    if (jsonInput) {
      structure = parseJsonStructure(jsonInput);
    } else if (wsdlInput) {
      structure = parseWsdlStructure(wsdlInput);
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

    const newStructure: DataStructure = {
      id: Date.now().toString(),
      name: structureName,
      type: jsonInput ? 'json' : wsdlInput ? 'wsdl' : xsdInput ? 'xsd' : edmxInput ? 'edmx' : 'custom',
      description: structureDescription,
      structure,
      createdAt: new Date().toISOString().split('T')[0],
      usage: structureUsage,
      namespace: (selectedStructureType === 'xsd' || selectedStructureType === 'wsdl' || selectedStructureType === 'edmx') && namespaceConfig.uri ? namespaceConfig : undefined
    };

    setStructures([...structures, newStructure]);
    
    toast({
      title: "Structure Saved",
      description: `Data structure "${structureName}" has been created successfully`,
    });

    return true;
  };

  const deleteStructure = (id: string) => {
    setStructures(structures.filter(s => s.id !== id));
    if (selectedStructure?.id === id) {
      setSelectedStructure(null);
    }
    toast({
      title: "Structure Deleted",
      description: "Data structure has been removed",
    });
  };

  const duplicateStructure = (structure: DataStructure) => {
    const duplicate = {
      ...structure,
      id: Date.now().toString(),
      name: `${structure.name} (Copy)`,
      createdAt: new Date().toISOString().split('T')[0]
    };
    setStructures([...structures, duplicate]);
    toast({
      title: "Structure Duplicated",
      description: `Created copy of "${structure.name}"`,
    });
  };

  return {
    structures,
    selectedStructure,
    setSelectedStructure,
    saveStructure,
    deleteStructure,
    duplicateStructure
  };
};