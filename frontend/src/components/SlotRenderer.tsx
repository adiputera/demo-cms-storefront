import type { Slot as SlotType } from '@/types';
import ComponentRenderer from '@/components/ComponentRenderer';

interface SlotRendererProps {
  slot: SlotType;
  className?: string;
}

export default function SlotRenderer({ slot, className = '' }: SlotRendererProps) {
  if (!slot.components || slot.components.length === 0) {
    return null;
  }

  return (
    <div className={`slot slot-${slot.code} ${className}`} data-slot-id={slot.id}>
      {slot.components
        .sort((a, b) => a.sortOrder - b.sortOrder)
        .map((component) => (
          <ComponentRenderer key={component.id} component={component} />
        ))}
    </div>
  );
}
