import { FC, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Plus, Trash2 } from 'lucide-react';

export interface FileFormatConfig {
  fileFormat?: 'CSV' | 'FIXED' | 'JSON' | 'XML' | 'TEXT';
  
  // CSV specific
  delimiter?: string;
  lineTerminator?: string;
  quoteCharacter?: string;
  includeHeaders?: boolean;
  quoteAllFields?: boolean;
  skipEmptyLines?: boolean;
  
  // Fixed-length specific
  fieldLengths?: Record<string, number>;
  fieldOrder?: string[];
  padCharacter?: string;
  padDirection?: 'LEFT' | 'RIGHT';
  
  // JSON specific
  prettyPrint?: boolean;
  includeNullValues?: boolean;
  dateFormat?: string;
  
  // XML specific (handled in XmlConversionTab)
  
  // Common
  encoding?: string;
}

interface FieldLength {
  fieldName: string;
  length: number;
  position?: number;
}

interface FileFormatTabProps {
  config: FileFormatConfig;
  onChange: (config: FileFormatConfig) => void;
}

export const FileFormatTab: FC<FileFormatTabProps> = ({ config, onChange }) => {
  const [newField, setNewField] = useState<FieldLength>({ fieldName: '', length: 0 });

  const handleChange = (field: keyof FileFormatConfig, value: any) => {
    onChange({
      ...config,
      [field]: value
    });
  };

  const handleAddField = () => {
    if (newField.fieldName && newField.length > 0) {
      const currentLengths = config.fieldLengths || {};
      const currentOrder = config.fieldOrder || [];
      
      onChange({
        ...config,
        fieldLengths: {
          ...currentLengths,
          [newField.fieldName]: newField.length
        },
        fieldOrder: [...currentOrder, newField.fieldName]
      });
      
      setNewField({ fieldName: '', length: 0 });
    }
  };

  const handleRemoveField = (fieldName: string) => {
    const { [fieldName]: _, ...remainingLengths } = config.fieldLengths || {};
    const remainingOrder = (config.fieldOrder || []).filter(f => f !== fieldName);
    
    onChange({
      ...config,
      fieldLengths: remainingLengths,
      fieldOrder: remainingOrder
    });
  };

  const renderFormatSpecificConfig = () => {
    switch (config.fileFormat) {
      case 'CSV':
        return (
          <Card>
            <CardHeader>
              <CardTitle>CSV Configuration</CardTitle>
              <CardDescription>Configure CSV file format settings</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 md:grid-cols-2">
                <div className="grid gap-2">
                  <Label htmlFor="delimiter">Delimiter</Label>
                  <Input
                    id="delimiter"
                    value={config.delimiter || ','}
                    onChange={(e) => handleChange('delimiter', e.target.value)}
                    placeholder=","
                    maxLength={1}
                  />
                </div>
                
                <div className="grid gap-2">
                  <Label htmlFor="quoteCharacter">Quote Character</Label>
                  <Input
                    id="quoteCharacter"
                    value={config.quoteCharacter || '"'}
                    onChange={(e) => handleChange('quoteCharacter', e.target.value)}
                    placeholder='"'
                    maxLength={1}
                  />
                </div>
                
                <div className="grid gap-2">
                  <Label htmlFor="lineTerminator">Line Terminator</Label>
                  <Select value={config.lineTerminator || '\\n'} onValueChange={(value) => handleChange('lineTerminator', value)}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="\n">LF (\n)</SelectItem>
                      <SelectItem value="\r\n">CRLF (\r\n)</SelectItem>
                      <SelectItem value="\r">CR (\r)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                
                <div className="grid gap-2">
                  <Label htmlFor="encoding">Encoding</Label>
                  <Select value={config.encoding || 'UTF-8'} onValueChange={(value) => handleChange('encoding', value)}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="UTF-8">UTF-8</SelectItem>
                      <SelectItem value="UTF-16">UTF-16</SelectItem>
                      <SelectItem value="ISO-8859-1">ISO-8859-1</SelectItem>
                      <SelectItem value="ASCII">ASCII</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              
              <div className="space-y-4">
                <div className="flex items-center space-x-2">
                  <Switch
                    id="includeHeaders"
                    checked={config.includeHeaders !== false}
                    onCheckedChange={(checked) => handleChange('includeHeaders', checked)}
                  />
                  <Label htmlFor="includeHeaders">Include Headers</Label>
                </div>
                
                <div className="flex items-center space-x-2">
                  <Switch
                    id="quoteAllFields"
                    checked={config.quoteAllFields === true}
                    onCheckedChange={(checked) => handleChange('quoteAllFields', checked)}
                  />
                  <Label htmlFor="quoteAllFields">Quote All Fields</Label>
                </div>
                
                <div className="flex items-center space-x-2">
                  <Switch
                    id="skipEmptyLines"
                    checked={config.skipEmptyLines === true}
                    onCheckedChange={(checked) => handleChange('skipEmptyLines', checked)}
                  />
                  <Label htmlFor="skipEmptyLines">Skip Empty Lines</Label>
                </div>
              </div>
            </CardContent>
          </Card>
        );
        
      case 'FIXED':
        return (
          <Card>
            <CardHeader>
              <CardTitle>Fixed-Length Configuration</CardTitle>
              <CardDescription>Define field positions and lengths</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 md:grid-cols-2">
                <div className="grid gap-2">
                  <Label htmlFor="padCharacter">Pad Character</Label>
                  <Input
                    id="padCharacter"
                    value={config.padCharacter || ' '}
                    onChange={(e) => handleChange('padCharacter', e.target.value)}
                    placeholder=" "
                    maxLength={1}
                  />
                </div>
                
                <div className="grid gap-2">
                  <Label htmlFor="padDirection">Pad Direction</Label>
                  <Select value={config.padDirection || 'RIGHT'} onValueChange={(value) => handleChange('padDirection', value)}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="LEFT">Left</SelectItem>
                      <SelectItem value="RIGHT">Right</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                
                <div className="grid gap-2">
                  <Label htmlFor="lineTerminator">Line Terminator</Label>
                  <Select value={config.lineTerminator || '\\n'} onValueChange={(value) => handleChange('lineTerminator', value)}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="\n">LF (\n)</SelectItem>
                      <SelectItem value="\r\n">CRLF (\r\n)</SelectItem>
                      <SelectItem value="\r">CR (\r)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                
                <div className="grid gap-2">
                  <Label htmlFor="encoding">Encoding</Label>
                  <Select value={config.encoding || 'UTF-8'} onValueChange={(value) => handleChange('encoding', value)}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="UTF-8">UTF-8</SelectItem>
                      <SelectItem value="UTF-16">UTF-16</SelectItem>
                      <SelectItem value="ISO-8859-1">ISO-8859-1</SelectItem>
                      <SelectItem value="ASCII">ASCII</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              
              <div className="space-y-4">
                <div>
                  <Label>Field Definitions</Label>
                  <div className="mt-2 space-y-2">
                    <div className="flex gap-2">
                      <Input
                        placeholder="Field Name"
                        value={newField.fieldName}
                        onChange={(e) => setNewField({ ...newField, fieldName: e.target.value })}
                      />
                      <Input
                        type="number"
                        placeholder="Length"
                        value={newField.length || ''}
                        onChange={(e) => setNewField({ ...newField, length: parseInt(e.target.value) || 0 })}
                        className="w-32"
                      />
                      <Button onClick={handleAddField} size="sm">
                        <Plus className="h-4 w-4" />
                      </Button>
                    </div>
                    
                    {config.fieldOrder && config.fieldOrder.length > 0 && (
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>Field Name</TableHead>
                            <TableHead>Length</TableHead>
                            <TableHead>Start Position</TableHead>
                            <TableHead className="w-16"></TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {config.fieldOrder.map((fieldName, index) => {
                            const startPos = config.fieldOrder!.slice(0, index).reduce(
                              (sum, fname) => sum + (config.fieldLengths?.[fname] || 0), 
                              1
                            );
                            return (
                              <TableRow key={fieldName}>
                                <TableCell>{fieldName}</TableCell>
                                <TableCell>{config.fieldLengths?.[fieldName]}</TableCell>
                                <TableCell>{startPos}</TableCell>
                                <TableCell>
                                  <Button
                                    variant="ghost"
                                    size="sm"
                                    onClick={() => handleRemoveField(fieldName)}
                                  >
                                    <Trash2 className="h-4 w-4" />
                                  </Button>
                                </TableCell>
                              </TableRow>
                            );
                          })}
                        </TableBody>
                      </Table>
                    )}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        );
        
      case 'JSON':
        return (
          <Card>
            <CardHeader>
              <CardTitle>JSON Configuration</CardTitle>
              <CardDescription>Configure JSON file format settings</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 md:grid-cols-2">
                <div className="grid gap-2">
                  <Label htmlFor="dateFormat">Date Format</Label>
                  <Input
                    id="dateFormat"
                    value={config.dateFormat || 'yyyy-MM-dd\'T\'HH:mm:ss.SSSZ'}
                    onChange={(e) => handleChange('dateFormat', e.target.value)}
                    placeholder="yyyy-MM-dd'T'HH:mm:ss.SSSZ"
                  />
                </div>
                
                <div className="grid gap-2">
                  <Label htmlFor="encoding">Encoding</Label>
                  <Select value={config.encoding || 'UTF-8'} onValueChange={(value) => handleChange('encoding', value)}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="UTF-8">UTF-8</SelectItem>
                      <SelectItem value="UTF-16">UTF-16</SelectItem>
                      <SelectItem value="ISO-8859-1">ISO-8859-1</SelectItem>
                      <SelectItem value="ASCII">ASCII</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              
              <div className="space-y-4">
                <div className="flex items-center space-x-2">
                  <Switch
                    id="prettyPrint"
                    checked={config.prettyPrint !== false}
                    onCheckedChange={(checked) => handleChange('prettyPrint', checked)}
                  />
                  <Label htmlFor="prettyPrint">Pretty Print</Label>
                </div>
                
                <div className="flex items-center space-x-2">
                  <Switch
                    id="includeNullValues"
                    checked={config.includeNullValues === true}
                    onCheckedChange={(checked) => handleChange('includeNullValues', checked)}
                  />
                  <Label htmlFor="includeNullValues">Include Null Values</Label>
                </div>
              </div>
            </CardContent>
          </Card>
        );
        
      case 'XML':
        return (
          <Card>
            <CardHeader>
              <CardTitle>XML Configuration</CardTitle>
              <CardDescription>XML format is handled in the XML Conversion tab</CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-muted-foreground">
                Please configure XML-specific settings in the XML Conversion tab.
              </p>
            </CardContent>
          </Card>
        );
        
      default:
        return (
          <Card>
            <CardHeader>
              <CardTitle>Plain Text Configuration</CardTitle>
              <CardDescription>Basic text file settings</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-2">
                <Label htmlFor="encoding">Encoding</Label>
                <Select value={config.encoding || 'UTF-8'} onValueChange={(value) => handleChange('encoding', value)}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="UTF-8">UTF-8</SelectItem>
                    <SelectItem value="UTF-16">UTF-16</SelectItem>
                    <SelectItem value="ISO-8859-1">ISO-8859-1</SelectItem>
                    <SelectItem value="ASCII">ASCII</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </CardContent>
          </Card>
        );
    }
  };

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>File Format Type</CardTitle>
          <CardDescription>Select the format of files this adapter will process</CardDescription>
        </CardHeader>
        <CardContent>
          <Select value={config.fileFormat || 'TEXT'} onValueChange={(value) => handleChange('fileFormat', value)}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="CSV">CSV (Comma-Separated Values)</SelectItem>
              <SelectItem value="FIXED">Fixed-Length</SelectItem>
              <SelectItem value="JSON">JSON</SelectItem>
              <SelectItem value="XML">XML</SelectItem>
              <SelectItem value="TEXT">Plain Text</SelectItem>
            </SelectContent>
          </Select>
        </CardContent>
      </Card>
      
      {renderFormatSpecificConfig()}
      
      <Card>
        <CardHeader>
          <CardTitle>Sample Preview</CardTitle>
          <CardDescription>Example of how your file format will look</CardDescription>
        </CardHeader>
        <CardContent>
          <pre className="text-xs bg-muted p-4 rounded overflow-x-auto">
            {config.fileFormat === 'CSV' && (
              config.includeHeaders !== false ? 
`orderId${config.delimiter || ','}customer${config.delimiter || ','}amount
12345${config.delimiter || ','}John Doe${config.delimiter || ','}99.99
12346${config.delimiter || ','}Jane Smith${config.delimiter || ','}149.99` :
`12345${config.delimiter || ','}John Doe${config.delimiter || ','}99.99
12346${config.delimiter || ','}Jane Smith${config.delimiter || ','}149.99`
            )}
            
            {config.fileFormat === 'FIXED' && config.fieldOrder && (
              config.fieldOrder.map(field => 
                field.padEnd(config.fieldLengths?.[field] || 0, config.padCharacter || ' ')
              ).join('') || 'Define fields above to see preview'
            )}
            
            {config.fileFormat === 'JSON' && (
              config.prettyPrint !== false ?
`{
  "orderId": "12345",
  "customer": "John Doe",
  "amount": 99.99${config.includeNullValues ? ',\n  "discount": null' : ''}
}` :
`{"orderId":"12345","customer":"John Doe","amount":99.99${config.includeNullValues ? ',"discount":null' : ''}}`
            )}
            
            {(config.fileFormat === 'XML' || config.fileFormat === 'TEXT' || !config.fileFormat) && 
              'Sample data based on selected format will appear here'
            }
          </pre>
        </CardContent>
      </Card>
    </div>
  );
};