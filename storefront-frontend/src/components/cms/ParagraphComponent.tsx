import type { ParagraphComponent as ParagraphComponentType } from '@/types';

export default function ParagraphComponent({
  title,
  content,
}: ParagraphComponentType) {
  return (
    <div className="prose max-w-none my-8">
      {title && <h2 className="text-3xl font-bold mb-4 text-gray-900">{title}</h2>}
      <div 
        className="text-gray-700 leading-relaxed"
        dangerouslySetInnerHTML={{ __html: content }}
      />
    </div>
  );
}
