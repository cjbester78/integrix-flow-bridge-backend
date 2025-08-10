import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { EmptyState } from '@/components/ui/empty-state';
import { Plus, Edit, Trash2, Key } from 'lucide-react';
import { Certificate } from '@/types/admin';

interface CertificateManagementProps {
  certificates: Certificate[];
}

export const CertificateManagement = ({ certificates }: CertificateManagementProps) => {
  const getStatusVariant = (status: string) => {
    switch (status) {
      case 'active':
        return 'default';
      case 'inactive':
        return 'secondary';
      case 'expiring':
        return 'destructive';
      default:
        return 'outline';
    }
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Certificate Management</CardTitle>
            <CardDescription>Manage SSL certificates and authentication keys</CardDescription>
          </div>
          <Button className="bg-gradient-primary">
            <Plus className="h-4 w-4 mr-2" />
            Add Certificate
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {certificates.length === 0 ? (
          <EmptyState
            icon={<Key className="h-12 w-12" />}
            title="No certificates found"
            description="Upload and manage SSL certificates and authentication keys for secure connections."
            action={{
              label: "Add Certificate",
              onClick: () => {/* Add certificate handler */}
            }}
          />
        ) : (
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead>Issuer</TableHead>
                  <TableHead>Valid Period</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Usage</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {certificates.map((cert) => (
                  <TableRow key={cert.id}>
                    <TableCell className="font-medium">{cert.name}</TableCell>
                    <TableCell>{cert.type}</TableCell>
                    <TableCell>{cert.issuer}</TableCell>
                    <TableCell className="text-sm">
                      {cert.validFrom} to {cert.validTo}
                    </TableCell>
                    <TableCell>
                      <Badge variant={getStatusVariant(cert.status)}>
                        {cert.status}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground text-sm">{cert.usage}</TableCell>
                    <TableCell>
                      <div className="flex gap-2">
                        <Button variant="ghost" size="sm">
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="sm" className="text-destructive">
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </CardContent>
    </Card>
  );
};