import React from 'react';

export default function MetricCard({ element, children }: any) {
  const { id, label, data } = element?.props ?? {};
  
  // Fake binding lookup since this is a UI stub
  const value = "120"; // Mock value

  return (
    <div className="bg-white rounded-lg border shadow-sm p-6 flex flex-col justify-center" id={id}>
      <h3 className="text-sm font-medium text-gray-500 mb-1">{label}</h3>
      <div className="text-3xl font-bold text-gray-900">{value}</div>
      {children}
    </div>
  );
}
