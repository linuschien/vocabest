import React from 'react';

export default function MetricCard({ element, children }: any) {
  const { id, label, data } = element?.props ?? {};
  
  // Fake binding lookup since this is a UI stub
  const value = element?.props?.value ?? "0";

  return (
    <div className="bg-card text-card-foreground rounded-lg border shadow-sm p-6 flex flex-col justify-center" id={id}>
      <h3 className="text-sm font-medium text-muted-foreground mb-1">{label}</h3>
      <div className="text-3xl font-bold">{value}</div>
      {children}
    </div>
  );
}
