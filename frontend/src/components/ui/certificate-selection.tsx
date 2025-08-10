import React, { useState, useEffect } from 'react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { certificateService } from '@/services/certificateService';
import { Certificate } from '@/types/admin';
import { toast } from '@/hooks/use-toast';
import { Skeleton } from '@/components/ui/skeleton';

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
  const [certificates, setCertificates] = useState<Certificate[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchCertificates = async () => {
      try {
        setIsLoading(true);
        const response = await certificateService.getAllCertificates(businessComponentId);
        
        if (response.success && response.data) {
          setCertificates(response.data);
        } else {
          console.error('Failed to fetch certificates:', response.error);
          setCertificates([]);
        }
      } catch (error) {
        console.error('Error fetching certificates:', error);
        setCertificates([]);
        toast({ title: "Error", description: 'Failed to load certificates', variant: "destructive" });
      } finally {
        setIsLoading(false);
      }
    };

    fetchCertificates();
  }, [businessComponentId]);

  // Only show active certificates
  const activeCertificates = certificates.filter(cert => cert.status === 'active');

  return (
    <div className="space-y-2">
      <Label htmlFor={id} className="flex items-center gap-1">
        {label}
        {required && <span className="text-destructive">*</span>}
      </Label>
      {isLoading ? (
        <Skeleton className="h-10 w-full" />
      ) : (
        <Select value={value} onValueChange={onChange}>
          <SelectTrigger id={id}>
            <SelectValue placeholder={placeholder} />
          </SelectTrigger>
          <SelectContent>
            {activeCertificates.length === 0 ? (
              <SelectItem value="" disabled>
                No certificates available {businessComponentId ? 'for this business component' : ''}
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
      )}
    </div>
  );
};