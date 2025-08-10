import React from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { AdapterConfiguration } from '@/types/adapter';

interface JdbcSenderAdapterConfigurationProps {
  configuration: AdapterConfiguration;
  onChange: (configuration: AdapterConfiguration) => void;
}

export const JdbcSenderAdapterConfiguration: React.FC<JdbcSenderAdapterConfigurationProps> = ({
  configuration,
  onChange,
}) => {
  const updateConfiguration = (updates: Partial<AdapterConfiguration>) => {
    onChange({ ...configuration, ...updates });
  };

  const updateProperties = (key: string, value: string) => {
    const properties = configuration.properties || {};
    updateConfiguration({
      properties: { ...properties, [key]: value }
    });
  };

  const getProperty = (key: string) => {
    return configuration.properties?.[key] || '';
  };

  return (
    <div className="space-y-6">
      <Tabs defaultValue="target" className="w-full">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="target">Target</TabsTrigger>
          <TabsTrigger value="processing">Processing</TabsTrigger>
        </TabsList>

        <TabsContent value="target" className="space-y-6">
          {/* Database Connection Section */}
          <Card>
            <CardHeader>
              <CardTitle>Database Connection</CardTitle>
              <CardDescription>
                Configure the JDBC database connection settings
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="jdbcDriverClass">JDBC Driver Class</Label>
                  <Input
                    id="jdbcDriverClass"
                    placeholder="oracle.jdbc.OracleDriver"
                    value={getProperty('jdbcDriverClass')}
                    onChange={(e) => updateProperties('jdbcDriverClass', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="jdbcUrl">JDBC URL</Label>
                  <Input
                    id="jdbcUrl"
                    placeholder="jdbc:oracle:thin:@dbhost:1521:orcl"
                    value={getProperty('jdbcUrl')}
                    onChange={(e) => updateProperties('jdbcUrl', e.target.value)}
                  />
                </div>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="databaseHost">Database Host</Label>
                  <Input
                    id="databaseHost"
                    placeholder="dbhost.example.com"
                    value={getProperty('databaseHost')}
                    onChange={(e) => updateProperties('databaseHost', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="databasePort">Database Port</Label>
                  <Input
                    id="databasePort"
                    type="number"
                    placeholder="1521"
                    value={getProperty('databasePort')}
                    onChange={(e) => updateProperties('databasePort', e.target.value)}
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="databaseName">Database Name</Label>
                  <Input
                    id="databaseName"
                    placeholder="salesdb"
                    value={getProperty('databaseName')}
                    onChange={(e) => updateProperties('databaseName', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="username">Username</Label>
                  <Input
                    id="username"
                    placeholder="dbuser"
                    value={getProperty('username')}
                    onChange={(e) => updateProperties('username', e.target.value)}
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="password">Password</Label>
                  <Input
                    id="password"
                    type="password"
                    placeholder="dbpassword"
                    value={getProperty('password')}
                    onChange={(e) => updateProperties('password', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="connectionPoolSettings">Connection Pool Settings</Label>
                  <Input
                    id="connectionPoolSettings"
                    placeholder="maxPoolSize=20, connectionTimeout=30000"
                    value={getProperty('connectionPoolSettings')}
                    onChange={(e) => updateProperties('connectionPoolSettings', e.target.value)}
                  />
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Insert/Update Logic Section */}
          <Card>
            <CardHeader>
              <CardTitle>Insert/Update Logic</CardTitle>
              <CardDescription>
                Configure how data is inserted or updated in the database
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="insertUpdateQuery">Insert/Update Query</Label>
                <Textarea
                  id="insertUpdateQuery"
                  placeholder="INSERT INTO sales (...) VALUES (...) or call stored proc"
                  value={getProperty('insertUpdateQuery')}
                  onChange={(e) => updateProperties('insertUpdateQuery', e.target.value)}
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="batchSize">Batch Size</Label>
                  <Input
                    id="batchSize"
                    type="number"
                    placeholder="100"
                    value={getProperty('batchSize')}
                    onChange={(e) => updateProperties('batchSize', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="commitInterval">Commit Interval</Label>
                  <Input
                    id="commitInterval"
                    placeholder="After every batch or N records"
                    value={getProperty('commitInterval')}
                    onChange={(e) => updateProperties('commitInterval', e.target.value)}
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="processing" className="space-y-6">
          {/* Data Preparation & Mapping Section */}
          <Card>
            <CardHeader>
              <CardTitle>Data Preparation & Mapping</CardTitle>
              <CardDescription>
                Configure data mapping and validation rules
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="dataMappingRules">Data Mapping Rules</Label>
                <Textarea
                  id="dataMappingRules"
                  placeholder="Map SaleID to sale_id"
                  value={getProperty('dataMappingRules')}
                  onChange={(e) => updateProperties('dataMappingRules', e.target.value)}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="validationRules">Validation Rules</Label>
                <Textarea
                  id="validationRules"
                  placeholder="Check mandatory fields"
                  value={getProperty('validationRules')}
                  onChange={(e) => updateProperties('validationRules', e.target.value)}
                />
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};