import React from 'react';
import { Renderer } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import spec from '@/schemas/critical-mission-log.render-schema.json';

export default function CriticalMissionLogPage() {
  return <Renderer spec={spec} registry={componentRegistry} />;
}
