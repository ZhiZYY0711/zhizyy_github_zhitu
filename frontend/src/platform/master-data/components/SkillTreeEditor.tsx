import { useEffect, useState } from 'react';
import { ChevronRightIcon, ChevronDownIcon } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { fetchSkillTree } from '../../services/api';
import type { SkillTreeNode } from '../../types';

function countNodes(nodes: SkillTreeNode[]): { categories: number; skills: number } {
  let categories = 0;
  let skills = 0;
  for (const node of nodes) {
    if (node.level === 1) categories++;
    else skills++;
    if (node.children?.length) {
      const sub = countNodes(node.children);
      categories += sub.categories;
      skills += sub.skills;
    }
  }
  return { categories, skills };
}

function TreeNode({ node, depth = 0 }: { node: SkillTreeNode; depth?: number }) {
  const [expanded, setExpanded] = useState(true);
  const hasChildren = node.children && node.children.length > 0;

  return (
    <div>
      <div
        className="flex items-center gap-2 py-1.5 px-2 rounded hover:bg-muted/50 transition-colors"
        style={{ paddingLeft: `${depth * 20 + 8}px` }}
      >
        {hasChildren ? (
          <button
            onClick={() => setExpanded(v => !v)}
            className="text-muted-foreground hover:text-foreground transition-colors flex-shrink-0"
          >
            {expanded
              ? <ChevronDownIcon className="h-4 w-4" />
              : <ChevronRightIcon className="h-4 w-4" />}
          </button>
        ) : (
          <span className="w-4 flex-shrink-0" />
        )}
        <span className="text-sm">{node.name}</span>
        <Badge variant="outline" className="text-xs ml-auto flex-shrink-0">
          L{node.level}
        </Badge>
      </div>
      {hasChildren && expanded && (
        <div>
          {node.children!.map(child => (
            <TreeNode key={child.id} node={child} depth={depth + 1} />
          ))}
        </div>
      )}
    </div>
  );
}

export default function SkillTreeEditor() {
  const [tree, setTree] = useState<SkillTreeNode[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSkillTree().then((data: SkillTreeNode[]) => {
      setTree(data);
      setLoading(false);
    });
  }, []);

  const { categories, skills } = countNodes(tree);

  return (
    <div className="space-y-4">
      {/* 统计信息 */}
      {loading ? (
        <Skeleton className="h-8 w-64" />
      ) : (
        <p className="text-sm text-muted-foreground">
          共 <strong>{categories}</strong> 个技能分类，<strong>{skills}</strong> 个具体技能
        </p>
      )}

      {/* 树形结构 */}
      <div className="border rounded-lg p-2">
        {loading ? (
          <div className="space-y-2 p-2">
            {[1, 2, 3, 4, 5].map(i => (
              <Skeleton key={i} className="h-8 w-full" style={{ width: `${100 - (i % 3) * 10}%` }} />
            ))}
          </div>
        ) : tree.length === 0 ? (
          <p className="text-center text-muted-foreground py-8">暂无技能树数据</p>
        ) : (
          tree.map(node => <TreeNode key={node.id} node={node} />)
        )}
      </div>
    </div>
  );
}
