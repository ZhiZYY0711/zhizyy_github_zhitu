import type {
  DashboardStats,
  SystemHealth,
  OnlineUserTrend,
  ServiceStatus,
  Tenant,
  EnterpriseAuditItem,
  ProjectAuditItem,
  Tag,
  SkillTreeNode,
  CertificateTemplate,
  ContractTemplate,
  RecommendationBanner,
  TopListItem,
  OperationLog,
  SecurityLog,
} from '../types';

// ── helpers ──────────────────────────────────────────────────────────────────
const ri = (min: number, max: number) => Math.floor(Math.random() * (max - min + 1)) + min;
const rf = (min: number, max: number, d = 3) => parseFloat((Math.random() * (max - min) + min).toFixed(d));
const pick = <T>(arr: readonly T[]): T => arr[Math.floor(Math.random() * arr.length)];
const uid = () => Math.random().toString(36).slice(2, 10);
const isoNow = () => new Date().toISOString();
const pastIso = (daysAgo: number) =>
  new Date(Date.now() - daysAgo * 86400_000).toISOString();
const futureIso = (daysAhead: number) =>
  new Date(Date.now() + daysAhead * 86400_000).toISOString();

// ── 1. DashboardStats ─────────────────────────────────────────────────────────
export const getMockDashboardStats = (): DashboardStats => ({
  total_users: ri(10000, 50000),
  total_colleges: ri(50, 200),
  total_enterprises: ri(200, 1000),
  active_projects: ri(100, 500),
  internship_positions: ri(500, 2000),
  pending_audits: ri(5, 30),
  system_health_score: ri(85, 99),
});

// ── 2. SystemHealth ───────────────────────────────────────────────────────────
export const getMockSystemHealth = (): SystemHealth => ({
  cpu_usage: rf(0.2, 0.8),
  memory_usage: rf(0.3, 0.75),
  disk_usage: rf(0.4, 0.7),
  active_services: ri(10, 15),
  error_rate: rf(0.001, 0.01, 4),
  online_users: ri(1000, 8000),
  timestamp: isoNow(),
});

// ── 3. OnlineUserTrend ────────────────────────────────────────────────────────
export const getMockOnlineUserTrend = (): OnlineUserTrend => {
  const now = Date.now();
  const data = Array.from({ length: 24 }, (_, i) => ({
    time: new Date(now - (23 - i) * 3600_000).toISOString(),
    count: ri(500, 5000),
  }));
  return { data, period: '24h' };
};

// ── 4. ServiceStatuses ────────────────────────────────────────────────────────
const SERVICE_NAMES = [
  'auth-service',
  'user-service',
  'training-service',
  'monitor-service',
  'system-service',
  'portal-platform-service',
  'notification-service',
  'file-service',
];

export const getMockServiceStatuses = (): ServiceStatus[] => {
  const count = ri(8, 12);
  const pool = [...SERVICE_NAMES];
  // pad with generated names if count > pool length
  while (pool.length < count) pool.push(`service-${uid()}`);

  return pool.slice(0, count).map((name) => ({
    name,
    status: Math.random() < 0.85 ? 'healthy' : pick(['degraded', 'down'] as const),
    response_time: ri(10, 200),
    last_check: isoNow(),
  }));
};

// ── 5. TenantList ─────────────────────────────────────────────────────────────
const COLLEGE_NAMES = [
  '北京大学', '清华大学', '复旦大学', '浙江大学', '南京大学',
  '武汉大学', '中山大学', '同济大学', '华中科技大学', '西安交通大学',
  '哈尔滨工业大学', '北京航空航天大学', '电子科技大学', '厦门大学', '山东大学',
];
const ENTERPRISE_NAMES = [
  '字节跳动科技有限公司', '腾讯科技（深圳）有限公司', '阿里巴巴网络技术有限公司',
  '华为技术有限公司', '百度在线网络技术（北京）有限公司', '京东科技控股股份有限公司',
  '美团科技有限公司', '滴滴出行科技有限公司', '网易（杭州）网络有限公司',
  '小米科技有限责任公司', '联想（北京）有限公司', '中兴通讯股份有限公司',
  '科大讯飞股份有限公司', '商汤科技开发有限公司', '旷视科技有限公司',
];

export const getMockTenantList = (): Tenant[] => {
  const count = ri(10, 20);
  return Array.from({ length: count }, (_, i) => {
    const type = Math.random() < 0.5 ? 'college' : 'enterprise';
    const name = type === 'college' ? pick(COLLEGE_NAMES) : pick(ENTERPRISE_NAMES);
    const status = pick(['active', 'active', 'active', 'inactive', 'pending'] as const);
    return {
      id: `tenant_${uid()}`,
      name,
      type,
      status,
      domain: `${name.slice(0, 4).toLowerCase().replace(/\s/g, '')}.edu.cn`,
      admin_username: `admin_${i + 1}`,
      admin_email: `admin${i + 1}@example.com`,
      max_students: type === 'college' ? ri(500, 5000) : undefined,
      expire_date: futureIso(ri(30, 365)),
      created_at: pastIso(ri(30, 730)),
      updated_at: pastIso(ri(0, 30)),
    };
  });
};

// ── 6. EnterpriseAuditList ────────────────────────────────────────────────────
export const getMockEnterpriseAuditList = (): EnterpriseAuditItem[] => {
  const count = ri(8, 15);
  const statuses = ['pending', 'pending', 'pending', 'approved', 'rejected'] as const;
  return Array.from({ length: count }, () => {
    const status = pick(statuses);
    const applyTime = pastIso(ri(1, 30));
    return {
      id: `ent_audit_${uid()}`,
      name: pick(ENTERPRISE_NAMES),
      license_url: `https://oss.example.com/license/${uid()}.jpg`,
      contact_person: pick(['张经理', '李总监', '王主任', '赵负责人', '陈联系人']),
      contact_phone: `1${ri(30, 99)}${ri(10000000, 99999999)}`,
      apply_time: applyTime,
      status,
      audit_time: status !== 'pending' ? pastIso(ri(0, 5)) : undefined,
      auditor: status !== 'pending' ? pick(['审核员A', '审核员B', '审核员C']) : undefined,
      reject_reason:
        status === 'rejected'
          ? pick(['营业执照信息不完整', '联系方式无法核实', '资质不符合要求'])
          : undefined,
    };
  });
};

// ── 7. ProjectAuditList ───────────────────────────────────────────────────────
const TECH_STACKS = [
  ['React', 'TypeScript', 'Node.js'],
  ['Vue 3', 'Vite', 'Spring Boot'],
  ['Python', 'Django', 'PostgreSQL'],
  ['Java', 'Spring Cloud', 'MySQL', 'Redis'],
  ['React Native', 'Expo', 'Firebase'],
  ['Flutter', 'Dart', 'Go'],
  ['Angular', 'NestJS', 'MongoDB'],
  ['Python', 'FastAPI', 'Docker', 'Kubernetes'],
];

export const getMockProjectAuditList = (): ProjectAuditItem[] => {
  const count = ri(6, 12);
  const statuses = ['pending', 'pending', 'approved', 'rejected'] as const;
  const ratings = ['S', 'A', 'B', 'C'] as const;
  return Array.from({ length: count }, () => {
    const status = pick(statuses);
    return {
      id: `proj_audit_${uid()}`,
      name: pick([
        '企业级电商平台实训项目',
        '智慧校园管理系统',
        '在线教育直播平台',
        '物流配送调度系统',
        '医疗健康数据平台',
        '金融风控分析系统',
        '社交媒体内容管理平台',
        '工业物联网监控系统',
      ]),
      provider: pick([...ENTERPRISE_NAMES, ...COLLEGE_NAMES]),
      provider_type: Math.random() < 0.6 ? 'enterprise' : 'college',
      tech_stack: pick(TECH_STACKS),
      description: '本项目旨在通过真实业务场景，帮助学生掌握企业级开发流程与技术实践。',
      difficulty: ri(1, 5),
      apply_time: pastIso(ri(1, 20)),
      status,
      quality_rating: status === 'approved' ? pick(ratings) : undefined,
      audit_comment:
        status !== 'pending'
          ? pick(['项目设计合理，技术选型恰当', '内容丰富，实践性强', '需补充更多技术细节'])
          : undefined,
    };
  });
};

// ── 8. Tags ───────────────────────────────────────────────────────────────────
const INDUSTRY_TAGS = ['互联网', '金融科技', '医疗健康', '教育科技', '电子商务', '人工智能', '云计算', '物联网', '游戏', '新能源'];
const TECH_TAGS = ['Java', 'Python', 'JavaScript', 'TypeScript', 'Go', 'Rust', 'React', 'Vue', 'Spring Boot', 'Docker', 'Kubernetes', 'MySQL', 'Redis', 'MongoDB'];
const SKILL_TAGS = ['算法设计', '系统架构', '前端开发', '后端开发', '数据分析', '机器学习', '项目管理', '代码审查', '性能优化', '安全开发'];

export const getMockTags = (): Tag[] => {
  const count = ri(20, 30);
  const allTags: { category: Tag['category']; name: string }[] = [
    ...INDUSTRY_TAGS.map((name) => ({ category: 'industry' as const, name })),
    ...TECH_TAGS.map((name) => ({ category: 'tech_stack' as const, name })),
    ...SKILL_TAGS.map((name) => ({ category: 'skill' as const, name })),
  ];

  // shuffle and take `count`
  const shuffled = allTags.sort(() => Math.random() - 0.5).slice(0, count);
  return shuffled.map((t, i) => ({
    id: `tag_${uid()}`,
    category: t.category,
    name: t.name,
    order: i + 1,
    created_at: pastIso(ri(30, 365)),
  }));
};

// ── 9. SkillTree ──────────────────────────────────────────────────────────────
const SKILL_TREE_DATA = [
  {
    name: '前端开发',
    children: [
      { name: 'HTML/CSS', skills: ['HTML5语义化', 'CSS3动画', 'Flexbox布局', 'Grid布局', '响应式设计'] },
      { name: 'JavaScript', skills: ['ES6+语法', '异步编程', 'DOM操作', '模块化', '设计模式'] },
      { name: 'React生态', skills: ['React Hooks', 'Redux', 'React Router', 'Next.js', 'React Query'] },
      { name: 'Vue生态', skills: ['Vue 3 Composition API', 'Pinia', 'Vue Router', 'Nuxt.js'] },
      { name: '工程化', skills: ['Webpack', 'Vite', 'ESLint', 'TypeScript', '单元测试'] },
    ],
  },
  {
    name: '后端开发',
    children: [
      { name: 'Java', skills: ['Java基础', 'Spring Boot', 'Spring Cloud', 'JVM调优', 'MyBatis'] },
      { name: 'Python', skills: ['Python基础', 'Django', 'FastAPI', 'Celery', '爬虫技术'] },
      { name: 'Go', skills: ['Go基础', 'Gin框架', 'gRPC', '并发编程', '微服务'] },
      { name: '数据库', skills: ['MySQL优化', 'PostgreSQL', 'Redis', 'MongoDB', 'Elasticsearch'] },
    ],
  },
  {
    name: '移动端开发',
    children: [
      { name: 'iOS', skills: ['Swift基础', 'UIKit', 'SwiftUI', 'Core Data', 'ARKit'] },
      { name: 'Android', skills: ['Kotlin基础', 'Jetpack Compose', 'Room', 'Retrofit', 'Coroutines'] },
      { name: '跨平台', skills: ['React Native', 'Flutter', 'Expo', 'Dart语言'] },
    ],
  },
  {
    name: '数据与AI',
    children: [
      { name: '数据分析', skills: ['Pandas', 'NumPy', 'Matplotlib', 'SQL分析', '数据可视化'] },
      { name: '机器学习', skills: ['Scikit-learn', '特征工程', '模型评估', '集成学习', '超参调优'] },
      { name: '深度学习', skills: ['PyTorch', 'TensorFlow', 'CNN', 'Transformer', '模型部署'] },
    ],
  },
  {
    name: 'DevOps',
    children: [
      { name: '容器化', skills: ['Docker', 'Docker Compose', 'Kubernetes', 'Helm', '镜像优化'] },
      { name: 'CI/CD', skills: ['GitHub Actions', 'Jenkins', 'GitLab CI', 'ArgoCD', '蓝绿部署'] },
      { name: '监控运维', skills: ['Prometheus', 'Grafana', 'ELK Stack', '链路追踪', '告警配置'] },
    ],
  },
];

export const getMockSkillTree = (): SkillTreeNode[] => {
  const topCount = ri(3, 5);
  return SKILL_TREE_DATA.slice(0, topCount).map((top, ti) => {
    const childCount = ri(3, Math.min(5, top.children.length));
    const children: SkillTreeNode[] = top.children.slice(0, childCount).map((mid, mi) => {
      const skillCount = ri(3, Math.min(6, mid.skills.length));
      const leaves: SkillTreeNode[] = mid.skills.slice(0, skillCount).map((skill, si) => ({
        id: `skill_${ti}_${mi}_${si}`,
        name: skill,
        level: 3,
        parent_id: `skill_${ti}_${mi}`,
      }));
      return {
        id: `skill_${ti}_${mi}`,
        name: mid.name,
        level: 2,
        parent_id: `skill_${ti}`,
        children: leaves,
      };
    });
    return {
      id: `skill_${ti}`,
      name: top.name,
      level: 1,
      children,
    };
  });
};

// ── 10. CertificateTemplates ──────────────────────────────────────────────────
export const getMockCertificateTemplates = (): CertificateTemplate[] => {
  const count = ri(3, 5);
  const names = ['实训结业证书', '优秀学员证书', '技能认证证书', '实习完成证书', '荣誉证书'];
  return names.slice(0, count).map((name, i) => ({
    id: `cert_tpl_${uid()}`,
    name,
    background_url: `https://oss.example.com/cert-bg/template_${i + 1}.png`,
    elements_layout: {
      student_name: { x: 400, y: 280, fontSize: 32 },
      issue_date: { x: 400, y: 380, fontSize: 18 },
      cert_id: { x: 400, y: 450, fontSize: 14 },
    },
    created_at: pastIso(ri(60, 365)),
    updated_at: pastIso(ri(0, 30)),
  }));
};

// ── 11. ContractTemplates ─────────────────────────────────────────────────────
export const getMockContractTemplates = (): ContractTemplate[] => {
  const count = ri(3, 5);
  const templates = [
    { name: '实习协议（标准版）', variables: ['student_name', 'enterprise_name', 'start_date', 'end_date', 'salary'] },
    { name: '实训合同（企业版）', variables: ['student_name', 'enterprise_name', 'project_name', 'duration'] },
    { name: '三方协议（学校版）', variables: ['student_name', 'college_name', 'enterprise_name', 'position'] },
    { name: '保密协议', variables: ['party_a', 'party_b', 'effective_date', 'scope'] },
    { name: '劳动合同（实习生）', variables: ['employee_name', 'employer_name', 'start_date', 'probation_period'] },
  ];
  const statuses = ['draft', 'active', 'archived'] as const;
  return templates.slice(0, count).map((t, i) => ({
    id: `contract_tpl_${uid()}`,
    name: t.name,
    version: `v${ri(1, 3)}.${ri(0, 9)}`,
    content: `本合同由甲方与乙方签订，约定如下条款：{{${t.variables.join('}}、{{')}}}...`,
    variables: t.variables,
    status: i === 0 ? 'draft' : pick(statuses),
    created_at: pastIso(ri(60, 365)),
  }));
};

// ── 12. RecommendationBanners ─────────────────────────────────────────────────
export const getMockRecommendationBanners = (): RecommendationBanner[] => {
  const count = ri(3, 6);
  const targetTypes = ['project', 'enterprise', 'course'] as const;
  return Array.from({ length: count }, (_, i) => ({
    id: `banner_${uid()}`,
    target_type: pick(targetTypes),
    target_id: uid(),
    title: pick([
      '热门实训项目推荐',
      '优质企业合作机会',
      '精品课程限时开放',
      '名企实习直通车',
      '技能提升专项训练营',
      '校企合作项目招募',
    ]),
    image_url: `https://oss.example.com/banners/banner_${i + 1}.jpg`,
    order: i + 1,
    status: Math.random() < 0.8 ? 'active' : 'inactive',
    start_date: pastIso(ri(0, 10)),
    end_date: futureIso(ri(10, 60)),
  }));
};

// ── 13. TopListItems ──────────────────────────────────────────────────────────
const TOP_MENTOR_NAMES = ['张明导师', '李华导师', '王芳导师', '陈刚导师', '刘洋导师', '赵磊导师', '孙静导师', '周鑫导师'];
const TOP_COURSE_NAMES = ['Java微服务实战', 'React全栈开发', 'Python数据分析', 'Docker容器化部署', 'Spring Cloud架构', 'Vue3企业级开发'];
const TOP_PROJECT_NAMES = ['电商平台全栈实训', '智慧城市大数据平台', '金融风控系统', '在线教育平台', '物流调度系统', '医疗信息化平台'];

export const getMockTopListItems = (listType: 'mentor' | 'course' | 'project'): TopListItem[] => {
  const count = ri(5, 10);
  const namePool =
    listType === 'mentor' ? TOP_MENTOR_NAMES : listType === 'course' ? TOP_COURSE_NAMES : TOP_PROJECT_NAMES;
  const reasons =
    listType === 'mentor'
      ? ['指导学生数量多', '学生评分高', '企业认可度高']
      : listType === 'course'
      ? ['完课率高', '好评率高', '技术前沿']
      : ['参与人数多', '项目质量高', '企业合作深度好'];

  return Array.from({ length: count }, (_, i) => ({
    id: `top_${uid()}`,
    list_type: listType,
    item_id: uid(),
    item_name: namePool[i % namePool.length],
    order: i + 1,
    reason: pick(reasons),
  }));
};

// ── 14. OperationLogs ─────────────────────────────────────────────────────────
const OP_ACTIONS = ['login', 'logout', 'create_tenant', 'update_tenant', 'delete_tenant', 'audit_enterprise', 'audit_project', 'update_config', 'reset_password', 'export_data', 'import_data', 'view_report'];
const OP_MODULES = ['auth', 'tenant', 'audit', 'system', 'config', 'report', 'user', 'resource'];
const OP_USERS = ['admin', 'audit_mgr', 'ops_mgr', 'devops_mgr', 'super_admin'];

export const getMockOperationLogs = (): OperationLog[] => {
  return Array.from({ length: 50 }, (_, i) => {
    const result = Math.random() < 0.9 ? 'success' : 'fail';
    return {
      id: `op_log_${uid()}`,
      user_id: `user_${ri(1, 20)}`,
      user_name: pick(OP_USERS),
      action: pick(OP_ACTIONS),
      module: pick(OP_MODULES),
      details: { request_id: uid(), duration_ms: ri(10, 500) },
      ip: `${ri(10, 220)}.${ri(0, 255)}.${ri(0, 255)}.${ri(1, 254)}`,
      user_agent: pick(['Chrome/120.0', 'Firefox/121.0', 'Safari/17.0', 'Edge/120.0']),
      time: new Date(Date.now() - (50 - i) * ri(60_000, 600_000)).toISOString(),
      result,
      error_message: result === 'fail' ? pick(['权限不足', '参数校验失败', '服务暂时不可用', '操作超时']) : undefined,
    };
  });
};

// ── 15. SecurityLogs ──────────────────────────────────────────────────────────
const SEC_EVENT_TYPES = ['login_fail', 'abnormal_access', 'permission_denied', 'data_breach'] as const;
const SEC_DESCRIPTIONS: Record<typeof SEC_EVENT_TYPES[number], string[]> = {
  login_fail: ['连续登录失败超过5次', '异地登录尝试', '使用已禁用账号登录'],
  abnormal_access: ['短时间内大量请求', '非工作时间访问敏感接口', '爬虫行为检测'],
  permission_denied: ['尝试访问无权限资源', '越权操作被拦截', '非法API调用'],
  data_breach: ['批量导出敏感数据', '异常数据查询', '数据泄露风险预警'],
};

export const getMockSecurityLogs = (): SecurityLog[] => {
  const levels = ['low', 'medium', 'high', 'critical'] as const;
  const levelWeights = [0.4, 0.35, 0.2, 0.05];

  const pickLevel = () => {
    const r = Math.random();
    let acc = 0;
    for (let i = 0; i < levels.length; i++) {
      acc += levelWeights[i];
      if (r < acc) return levels[i];
    }
    return 'low';
  };

  return Array.from({ length: 20 }, (_, i) => {
    const event_type = pick(SEC_EVENT_TYPES);
    const level = pickLevel();
    return {
      id: `sec_log_${uid()}`,
      event_type,
      level,
      description: pick(SEC_DESCRIPTIONS[event_type]),
      user_id: Math.random() < 0.7 ? `user_${ri(1, 100)}` : undefined,
      ip: `${ri(10, 220)}.${ri(0, 255)}.${ri(0, 255)}.${ri(1, 254)}`,
      time: new Date(Date.now() - (20 - i) * ri(300_000, 3_600_000)).toISOString(),
      handled: Math.random() < 0.6,
    };
  });
};
