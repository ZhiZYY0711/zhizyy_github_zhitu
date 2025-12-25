import React from 'react';
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { ScrollText, ExternalLink, CheckCircle } from 'lucide-react';
import type { Certificate } from '../../mock/generator';

interface CertificateCardProps {
  certificate: Certificate;
}

const CertificateCard: React.FC<CertificateCardProps> = ({ certificate }) => {
  return (
    <Card className="overflow-hidden border-l-4 border-l-blue-500 hover:shadow-md transition-shadow">
      <CardContent className="p-0">
        <div className="flex flex-col md:flex-row">
          <div className="bg-blue-50 dark:bg-blue-950/30 p-6 flex items-center justify-center min-w-[120px]">
            <ScrollText className="h-10 w-10 text-blue-500" />
          </div>
          <div className="p-6 flex-1 space-y-2">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="font-bold text-lg">{certificate.title}</h3>
                <p className="text-sm text-muted-foreground">颁发机构: {certificate.issuer}</p>
              </div>
              <Badge variant="outline" className="border-green-500 text-green-600 bg-green-50 dark:bg-green-950/20">
                <CheckCircle className="h-3 w-3 mr-1" />
                {certificate.status === 'valid' ? '有效' : '已撤销'}
              </Badge>
            </div>

            <div className="flex items-center gap-4 text-sm text-muted-foreground mt-4">
              <span>颁发日期: {certificate.issue_date}</span>
            </div>

            <div className="bg-slate-100 dark:bg-slate-800 p-2 rounded text-xs font-mono break-all mt-2">
              Hash: {certificate.hash}
            </div>

            <div className="pt-2">
              <Button variant="outline" size="sm" className="gap-2">
                <ExternalLink className="h-3 w-3" />
                区块链存证查验
              </Button>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default CertificateCard;
