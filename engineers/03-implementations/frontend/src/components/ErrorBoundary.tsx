import React, { Component, ErrorInfo, ReactNode } from 'react';

interface Props {
  children?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Uncaught error:', error, errorInfo);
  }

  public render() {
    if (this.state.hasError) {
      return (
        <div className="p-8 m-4 bg-red-100 text-red-900 border border-red-300 rounded overflow-auto max-w-full">
          <h1 className="text-xl font-bold mb-2">Something went wrong.</h1>
          <pre className="whitespace-pre-wrap font-mono text-sm">{this.state.error?.message}</pre>
          <pre className="whitespace-pre-wrap font-mono text-xs mt-4">{this.state.error?.stack}</pre>
        </div>
      );
    }

    return this.props.children;
  }
}
