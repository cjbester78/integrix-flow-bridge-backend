import { useState } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { EmptyState } from '@/components/ui/empty-state';
import { LoadingSkeleton } from '@/components/ui/loading-skeleton';
import { cn } from '@/lib/utils';
import { MoreHorizontal, Search, LucideIcon } from 'lucide-react';

export interface DataTableColumn<T> {
  key: string;
  header: string;
  cell?: (item: T) => React.ReactNode;
  className?: string;
  sortable?: boolean;
}

export interface DataTableAction<T> {
  label: string;
  icon?: LucideIcon;
  onClick: (item: T) => void;
  variant?: 'default' | 'destructive';
  show?: (item: T) => boolean;
}

interface DataTableProps<T> {
  columns: DataTableColumn<T>[];
  data: T[];
  actions?: DataTableAction<T>[];
  keyField: keyof T;
  loading?: boolean;
  searchable?: boolean;
  searchPlaceholder?: string;
  emptyState?: {
    icon?: LucideIcon;
    title: string;
    description?: string;
    action?: {
      label: string;
      onClick: () => void;
    };
  };
  className?: string;
  toolbar?: React.ReactNode;
}

export function DataTable<T extends Record<string, any>>({
  columns,
  data,
  actions,
  keyField,
  loading = false,
  searchable = false,
  searchPlaceholder = "Search...",
  emptyState,
  className,
  toolbar
}: DataTableProps<T>) {
  const [searchQuery, setSearchQuery] = useState('');
  
  // Filter data based on search query
  const filteredData = searchQuery
    ? data.filter(item => 
        columns.some(column => {
          const value = column.cell ? column.cell(item) : item[column.key];
          return String(value).toLowerCase().includes(searchQuery.toLowerCase());
        })
      )
    : data;

  if (loading) {
    return <LoadingSkeleton variant="table" />;
  }

  return (
    <div className={cn("space-y-4", className)}>
      {(searchable || toolbar) && (
        <div className="flex items-center justify-between gap-4">
          {searchable && (
            <div className="relative max-w-sm">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder={searchPlaceholder}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
          )}
          {toolbar}
        </div>
      )}
      
      <div className="rounded-md border overflow-x-auto">
        <Table>
          <TableHeader>
            <TableRow>
              {columns.map((column) => (
                <TableHead key={column.key} className={column.className}>
                  {column.header}
                </TableHead>
              ))}
              {actions && actions.length > 0 && (
                <TableHead className="w-[70px]">Actions</TableHead>
              )}
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredData.length === 0 ? (
              <TableRow>
                <TableCell 
                  colSpan={columns.length + (actions ? 1 : 0)} 
                  className="h-32 text-center"
                >
                  {emptyState ? (
                    <EmptyState
                      icon={emptyState.icon}
                      title={emptyState.title}
                      description={emptyState.description}
                      action={emptyState.action}
                    />
                  ) : (
                    <p className="text-muted-foreground">No data available</p>
                  )}
                </TableCell>
              </TableRow>
            ) : (
              filteredData.map((item) => (
                <TableRow 
                  key={String(item[keyField])}
                  className="hover:bg-muted/50 transition-colors"
                >
                  {columns.map((column) => (
                    <TableCell key={column.key} className={column.className}>
                      {column.cell ? column.cell(item) : item[column.key]}
                    </TableCell>
                  ))}
                  {actions && actions.length > 0 && (
                    <TableCell>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-8 w-8"
                            aria-label="More actions"
                          >
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          {actions.map((action, index) => {
                            if (action.show && !action.show(item)) {
                              return null;
                            }
                            
                            const isLast = index === actions.length - 1;
                            const isDestructive = action.variant === 'destructive';
                            
                            return (
                              <div key={action.label}>
                                {isDestructive && index > 0 && (
                                  <DropdownMenuSeparator />
                                )}
                                <DropdownMenuItem
                                  onClick={() => action.onClick(item)}
                                  className={cn(
                                    isDestructive && "text-destructive focus:text-destructive"
                                  )}
                                >
                                  {action.icon && (
                                    <action.icon className="h-4 w-4 mr-2" />
                                  )}
                                  {action.label}
                                </DropdownMenuItem>
                                {isDestructive && !isLast && (
                                  <DropdownMenuSeparator />
                                )}
                              </div>
                            );
                          })}
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  )}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}