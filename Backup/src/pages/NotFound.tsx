import { useLocation } from "react-router-dom";
import { useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { AlertTriangle } from "lucide-react";

const NotFound = () => {
  const location = useLocation();

  useEffect(() => {
    console.error(
      "404 Error: User attempted to access non-existent route:",
      location.pathname
    );
  }, [location.pathname]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-secondary">
      <Card className="w-full max-w-md shadow-elegant text-center">
        <CardHeader className="space-y-4">
          <div className="flex items-center justify-center">
            <div className="h-12 w-12 rounded-xl bg-gradient-primary flex items-center justify-center">
              <AlertTriangle className="h-6 w-6 text-primary-foreground" />
            </div>
          </div>
          <CardTitle className="text-4xl font-bold text-foreground">404</CardTitle>
          <CardDescription className="text-xl">Oops! Page not found</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground mb-6">
            The page you're looking for doesn't exist or has been moved.
          </p>
          <Button asChild className="bg-gradient-primary hover:opacity-90">
            <a href="/dashboard">Return to Dashboard</a>
          </Button>
        </CardContent>
      </Card>
    </div>
  );
};

export default NotFound;
