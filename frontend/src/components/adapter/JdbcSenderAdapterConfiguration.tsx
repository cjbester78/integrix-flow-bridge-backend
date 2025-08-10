import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
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
      <Tabs defaultValue="source" className="w-full">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="source">Source</TabsTrigger>
          <TabsTrigger value="processing">Processing</TabsTrigger>
        </TabsList>

        <TabsContent value="source" className="space-y-6">
          {/* Database Connection Section */}
          <Card>
            <CardHeader>
              <CardTitle>Source Database Connection</CardTitle>
              <CardDescription>
                Configure connection to the source database for polling and data retrieval
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

          {/* Data Retrieval and Polling Section */}
          <Card>
            <CardHeader>
              <CardTitle>Data Retrieval and Polling</CardTitle>
              <CardDescription>
                Configure how data is retrieved from the source database
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="selectQuery">Select Query</Label>
                <Textarea
                  id="selectQuery"
                  placeholder="SELECT * FROM orders WHERE created_date > ? ORDER BY id"
                  value={getProperty('selectQuery')}
                  onChange={(e) => updateProperties('selectQuery', e.target.value)}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="countQuery">Count Query (Optional)</Label>
                <Textarea
                  id="countQuery"
                  placeholder="SELECT COUNT(*) FROM orders WHERE created_date > ?"
                  value={getProperty('countQuery')}
                  onChange={(e) => updateProperties('countQuery', e.target.value)}
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="pollingInterval">Polling Interval (ms)</Label>
                  <Input
                    id="pollingInterval"
                    type="number"
                    placeholder="30000"
                    value={getProperty('pollingInterval')}
                    onChange={(e) => updateProperties('pollingInterval', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="fetchSize">Fetch Size</Label>
                  <Input
                    id="fetchSize"
                    type="number"
                    placeholder="1000"
                    value={getProperty('fetchSize')}
                    onChange={(e) => updateProperties('fetchSize', e.target.value)}
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="incrementalColumn">Incremental Column</Label>
                  <Input
                    id="incrementalColumn"
                    placeholder="created_date, updated_timestamp, id"
                    value={getProperty('incrementalColumn')}
                    onChange={(e) => updateProperties('incrementalColumn', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="maxResults">Max Results per Poll</Label>
                  <Input
                    id="maxResults"
                    type="number"
                    placeholder="1000"
                    value={getProperty('maxResults')}
                    onChange={(e) => updateProperties('maxResults', e.target.value)}
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