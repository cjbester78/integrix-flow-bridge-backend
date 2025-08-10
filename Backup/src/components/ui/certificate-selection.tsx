import React from 'react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { useBusinessComponentAdapters } from '@/hooks/useBusinessComponentAdapters';

interface Certificate {
  id: string;
  name: string;
  type: string;
  issuer: string;
  status: string;
  businessComponentId?: string;
}

interface CertificateSelectionProps {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  businessComponentId?: string;
  placeholder?: string;
  required?: boolean;
}

export const CertificateSelection: React.FC<CertificateSelectionProps> = ({
  id,
  label,
  value,
  onChange,
  businessComponentId,
  placeholder = "Select certificate",
  required = false
}) => {
  // Mock certificates for now - in real implementation this would come from an API
  const mockCertificates: Certificate[] = [
    {
      id: 'cert-1',
      name: 'Production SSL Certificate',
      type: 'SSL',
      issuer: 'DigiCert Inc',
      status: 'active',
      businessComponentId: 'bc-1'
    },
    {
      id: 'cert-2', 
      name: 'Client Authentication Certificate',
      type: 'Client Auth',
      issuer: 'Internal CA',
      status: 'active',
      businessComponentId: 'bc-1'
    },
    {
      id: 'cert-3',
      name: 'Development SSL Certificate',
      type: 'SSL',
      issuer: 'Let\'s Encrypt',
      status: 'active',
      businessComponentId: 'bc-2'
    }
  ];

  // Filter certificates by business component if provided
  const filteredCertificates = businessComponentId 
    ? mockCertificates.filter(cert => cert.businessComponentId === businessComponentId)
    : mockCertificates;

  // Only show active certificates
  const activeCertificates = filteredCertificates.filter(cert => cert.status === 'active');

  return (
    <div className="space-y-2">
      <Label htmlFor={id} className="flex items-center gap-1">
        {label}
        {required && <span className="text-destructive">*</span>}
      </Label>
      <Select value={value} onValueChange={onChange}>
        <SelectTrigger id={id}>
          <SelectValue placeholder={placeholder} />
        </SelectTrigger>
        <SelectContent>
          {activeCertificates.length === 0 ? (
            <SelectItem value="" disabled>
              No certificates available for this business component
            </SelectItem>
          ) : (
            activeCertificates.map((cert) => (
              <SelectItem key={cert.id} value={cert.id}>
                <div className="flex flex-col">
                  <span className="font-medium">{cert.name}</span>
                  <span className="text-xs text-muted-foreground">
                    {cert.type} • {cert.issuer}
                  </span>
                </div>
              </SelectItem>
            ))
          )}
        </SelectContent>
      </Select>
    </div>
  );
};