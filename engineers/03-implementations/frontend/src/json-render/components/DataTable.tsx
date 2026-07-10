import React, { useState } from 'react';
import { useStateStore, useStateValue } from '@json-render/react';
import { useNavigate } from 'react-router-dom';

export default function DataTable({ element, children, emit, on }: any) {
  const store = useStateStore();
  const navigate = useNavigate();
  const { id, label, columns, data, rowActions, pageSize = 10, totalElements: totalElementsProp } = element?.props ?? {};
  
  const bindStateValue = useStateValue(data?.$bindState);
  const totalElementsBindValue = useStateValue(totalElementsProp?.$bindState);
  
  const [currentPage, setCurrentPage] = useState(1);
  
  // Resolve data via store binding
  let rows: any[] = [];
  if (data && data.$bindState) {
    rows = (bindStateValue as any[]) || [];
  } else if (Array.isArray(data)) {
    rows = data;
  }

  const userRole = store.get('/data/user/role');
  const visibleActions = rowActions?.filter((action: any) => !(action.adminOnly && userRole !== 'ADMIN')) || [];

  const handleActionClick = (action: any, row: any) => {
    store.set('/data/activeWordBank', row);
    store.set('/data/activeWordBankId', row.id);
    
    if (action.type === 'navigate') {
      navigate(action.path);
    } else if (action.type === 'modal') {
      store.set(`/modals/${action.modalId}`, true);
    }
  };

  const isServerSide = totalElementsBindValue !== undefined;
  const totalElements = isServerSide ? (totalElementsBindValue as number) : rows.length;

  const totalPages = Math.max(1, Math.ceil(totalElements / pageSize));
  const startIndex = (currentPage - 1) * pageSize;
  const paginatedRows = isServerSide ? rows : rows.slice(startIndex, startIndex + pageSize);

  const handlePageChange = (newPage: number) => {
    setCurrentPage(newPage);
    if (emit) {
      emit('pageChange', { page: newPage });
    }
  };

  return (
    <div className="border border-border rounded-lg shadow-sm bg-card overflow-hidden" id={id}>
      {label && <div className="px-4 py-3 border-b border-border bg-muted/50 font-semibold text-foreground">{label}</div>}
      <table className="w-full text-sm text-left text-muted-foreground">
        <thead className="text-xs text-foreground uppercase bg-muted/50">
          <tr>
            <th className="px-6 py-3 select-none w-16">#</th>
            {columns?.map((col: any) => (
              <th key={col.field} className="px-6 py-3 cursor-pointer select-none">
                {col.label} {col.sortable ? '↕️' : ''}
              </th>
            ))}
            {(children?.length > 0 || visibleActions.length > 0) && <th className="px-6 py-3 text-right">Actions</th>}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr><td colSpan={columns?.length + (children?.length || visibleActions.length > 0 ? 2 : 1)} className="px-6 py-4 text-center">(沒有資料)</td></tr>
          ) : (
            paginatedRows.map((row: any, i: number) => (
              <tr key={row.id || i} className="bg-card border-b border-border hover:bg-muted/50">
                <td className="px-6 py-4 font-medium text-muted-foreground whitespace-nowrap">
                  {startIndex + i + 1}
                </td>
                {columns?.map((col: any) => (
                  <td key={col.field} className="px-6 py-4 font-medium text-card-foreground whitespace-nowrap">
                    {row[col.field]}
                  </td>
                ))}
                {(children?.length > 0 || visibleActions.length > 0) && (
                  <td className="px-6 py-4 text-right flex justify-end gap-2 items-center">
                    {children}
                    {visibleActions.map((action: any) => {
                      const btnClass = action.variant === 'danger' 
                        ? 'text-destructive hover:text-destructive/80' 
                        : 'text-primary hover:text-primary/80';
                        
                      return (
                        <button 
                          key={action.id} 
                          onClick={() => handleActionClick(action, row)}
                          className={`px-3 py-1 text-xs font-medium rounded border border-input ${btnClass} hover:bg-accent transition-colors`}
                        >
                          {action.label}
                        </button>
                      );
                    })}
                  </td>
                )}
              </tr>
            ))
          )}
        </tbody>
      </table>
      
      {totalElements > pageSize && (
        <div className="flex items-center justify-between px-4 py-3 bg-card border-t border-border sm:px-6">
          <div className="flex justify-between flex-1 sm:hidden">
            <button
              onClick={() => handlePageChange(Math.max(1, currentPage - 1))}
              disabled={currentPage === 1}
              className="relative inline-flex items-center px-4 py-2 text-sm font-medium text-foreground bg-background border border-input rounded-md hover:bg-accent disabled:opacity-50"
            >
              Previous
            </button>
            <button
              onClick={() => handlePageChange(Math.min(totalPages, currentPage + 1))}
              disabled={currentPage === totalPages}
              className="relative ml-3 inline-flex items-center px-4 py-2 text-sm font-medium text-foreground bg-background border border-input rounded-md hover:bg-accent disabled:opacity-50"
            >
              Next
            </button>
          </div>
          <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
            <div>
              <p className="text-sm text-muted-foreground">
                Showing <span className="font-medium text-foreground">{totalElements === 0 ? 0 : startIndex + 1}</span> to <span className="font-medium text-foreground">{Math.min(startIndex + pageSize, totalElements)}</span> of <span className="font-medium text-foreground">{totalElements}</span> results
              </p>
            </div>
            <div>
              <nav className="inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
                <button
                  onClick={() => handlePageChange(Math.max(1, currentPage - 1))}
                  disabled={currentPage === 1}
                  className="relative inline-flex items-center px-2 py-2 text-muted-foreground rounded-l-md ring-1 ring-inset ring-input hover:bg-accent focus:z-20 focus:outline-offset-0 disabled:opacity-50"
                >
                  <span className="sr-only">Previous</span>
                  <svg className="w-5 h-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                    <path fillRule="evenodd" d="M12.79 5.23a.75.75 0 01-.02 1.06L8.832 10l3.938 3.71a.75.75 0 11-1.04 1.08l-4.5-4.25a.75.75 0 010-1.08l4.5-4.25a.75.75 0 011.06.02z" clipRule="evenodd" />
                  </svg>
                </button>
                <span className="relative inline-flex items-center px-4 py-2 text-sm font-semibold text-foreground ring-1 ring-inset ring-input focus:outline-offset-0">
                  Page {currentPage} of {totalPages}
                </span>
                <button
                  onClick={() => handlePageChange(Math.min(totalPages, currentPage + 1))}
                  disabled={currentPage === totalPages}
                  className="relative inline-flex items-center px-2 py-2 text-muted-foreground rounded-r-md ring-1 ring-inset ring-input hover:bg-accent focus:z-20 focus:outline-offset-0 disabled:opacity-50"
                >
                  <span className="sr-only">Next</span>
                  <svg className="w-5 h-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                    <path fillRule="evenodd" d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z" clipRule="evenodd" />
                  </svg>
                </button>
              </nav>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
