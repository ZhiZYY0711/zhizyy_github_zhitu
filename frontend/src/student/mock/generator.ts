// f:\projects\zhitu\frontend\src\student\mock\generator.ts

export interface DashboardStats {
  gpa: number;
  credit_completed: number;
  training_hours: number;
  internship_status: 0 | 1 | 2 | 3;
  radar_summary: {
    labels: string[];
    data: number[];
  };
  pending_tasks_count: number;
}

export interface RadarData {
  dimensions: {
    key: string;
    label: string;
    score: number;
    max: number;
  }[];
  peer_average: number[];
  history: {
    date: string;
    avg_score: number;
  }[];
}

export interface TaskItem {
  id: string;
  type: 'weekly_report' | 'interview_invite' | 'training_submit';
  title: string;
  deadline: string;
  priority: 'high' | 'medium' | 'low';
  jump_url: string;
}

export interface TaskResponse {
  total: number;
  records: TaskItem[];
}

export interface RecommendationItem {
  rec_id: string;
  type: 'project' | 'job' | 'course';
  title: string;
  tags?: string[];
  company_name?: string;
  match_reason: string;
  match_score: number;
  target_id: string;
}

// Training Module Interfaces
export interface Project {
  id: string;
  name: string;
  provider: string;
  difficulty: number; // 1-5
  tech_stack: string[];
  status: 'recruiting' | 'ongoing' | 'finished';
  team_size: number;
  current_members: number;
  description?: string;
}

// Backend API response types
export interface ProjectApiResponse {
  id: number;
  projectName: string;
  description: string;
  techStack: string[];
  industry: string;
  maxTeams: number;
  maxMembers: number;
  startDate: string;
  endDate: string;
  status: number; // 1=recruiting, 2=ongoing, 3=finished
  enrollmentStatus: number | null;
  createdAt: string;
}

export interface PaginatedResponse<T> {
  total: number;
  records: T[];
  page: number;
  size: number;
}

export interface ScrumTask {
  id: string;
  title: string;
  assignee: string;
  story_points: number;
  priority: 'high' | 'medium' | 'low';
}

export interface ScrumBoard {
  sprint_name: string;
  columns: {
    todo: ScrumTask[];
    in_progress: ScrumTask[];
    review: ScrumTask[];
    done: ScrumTask[];
  };
}

// Random helper functions
const getRandomInt = (min: number, max: number) => Math.floor(Math.random() * (max - min + 1)) + min;
const getRandomFloat = (min: number, max: number, decimals: number = 1) => parseFloat((Math.random() * (max - min) + min).toFixed(decimals));
const getRandomArrayItem = <T>(arr: T[]): T => arr[Math.floor(Math.random() * arr.length)];

export const getMockDashboardStats = (): DashboardStats => {
  return {
    gpa: getRandomFloat(2.0, 4.0, 2),
    credit_completed: getRandomInt(80, 150),
    training_hours: getRandomInt(100, 500),
    internship_status: getRandomArrayItem([0, 1, 2, 3]),
    radar_summary: {
      labels: ["技术", "沟通", "管理", "文档", "创新", "学习"],
      data: Array.from({ length: 6 }, () => getRandomInt(50, 95))
    },
    pending_tasks_count: getRandomInt(0, 10)
  };
};

export const getMockRadarData = (): RadarData => {
  const dimensions = [
    { key: "tech", label: "技术能力" },
    { key: "comm", label: "沟通协作" },
    { key: "mgmt", label: "项目管理" },
    { key: "doc", label: "文档编写" },
    { key: "inno", label: "创新思维" },
    { key: "learn", label: "学习能力" }
  ];

  return {
    dimensions: dimensions.map(d => ({
      ...d,
      score: getRandomInt(60, 95),
      max: 100
    })),
    peer_average: Array.from({ length: 6 }, () => getRandomInt(50, 85)),
    history: [
      { date: "2023-09", avg_score: getRandomInt(60, 70) },
      { date: "2024-03", avg_score: getRandomInt(70, 85) }
    ]
  };
};

export const getMockTasks = (): TaskResponse => {
  const count = getRandomInt(1, 5);
  const tasks: TaskItem[] = [];
  const types: TaskItem['type'][] = ['weekly_report', 'interview_invite', 'training_submit'];
  const priorities: TaskItem['priority'][] = ['high', 'medium', 'low'];

  for (let i = 0; i < count; i++) {
    const type = getRandomArrayItem(types);
    let title = "";
    if (type === 'weekly_report') title = `第${getRandomInt(1, 10)}周实习周报待提交`;
    else if (type === 'interview_invite') title = `字节跳动-后端开发实习生 面试邀约`;
    else title = `实训项目阶段成果提交`;

    tasks.push({
      id: `task_${getRandomInt(100, 999)}`,
      type,
      title,
      deadline: new Date(Date.now() + getRandomInt(1, 7) * 24 * 60 * 60 * 1000).toISOString(),
      priority: getRandomArrayItem(priorities),
      jump_url: '#'
    });
  }

  return {
    total: count,
    records: tasks
  };
};

export const getMockRecommendations = (): RecommendationItem[] => {
  const items: RecommendationItem[] = [
    {
      rec_id: "r_1001",
      type: "project",
      title: "基于Spring Cloud的电商系统实训",
      tags: ["Java", "微服务", "企业级"],
      match_reason: "匹配你的Java技术栈，且你缺少微服务项目经验",
      match_score: 0.95,
      target_id: "proj_888"
    },
    {
      rec_id: "r_1002",
      type: "job",
      title: "前端开发实习生",
      company_name: "美团",
      match_reason: "你的React能力评级为A，符合岗位要求",
      match_score: 0.88,
      target_id: "job_999"
    },
    {
      rec_id: "r_1003",
      type: "course",
      title: "高级算法与数据结构",
      match_reason: "提升算法能力，备战大厂面试",
      match_score: 0.92,
      target_id: "course_777"
    }
  ];
  // Randomly return 1-3 items
  return items.slice(0, getRandomInt(1, 3));
};

export const getMockProjects = (): Project[] => {
  const projects: Project[] = [
    {
      id: "proj_001",
      name: "银行核心交易系统仿真",
      provider: "某国有银行科技部",
      difficulty: 4,
      tech_stack: ["Java", "Oracle", "Redis"],
      status: "recruiting",
      team_size: 5,
      current_members: 2,
      description: "模拟银行核心交易流程，包括账户管理、转账汇款、交易流水记录等模块，高并发场景设计。"
    },
    {
      id: "proj_002",
      name: "企业级CRM客户管理系统",
      provider: "Salesforce Partner",
      difficulty: 3,
      tech_stack: ["React", "Node.js", "PostgreSQL"],
      status: "recruiting",
      team_size: 4,
      current_members: 1,
      description: "为中小企业打造的客户关系管理系统，包含销售漏斗、客户画像、营销自动化等功能。"
    },
    {
      id: "proj_003",
      name: "智慧城市交通流量监控平台",
      provider: "阿里云实训组",
      difficulty: 5,
      tech_stack: ["Python", "Spark", "ECharts"],
      status: "ongoing",
      team_size: 6,
      current_members: 6,
      description: "基于实时交通数据的大屏可视化监控系统，涉及大数据流处理与复杂前端可视化。"
    },
    {
      id: "proj_004",
      name: "医疗影像AI辅助诊断系统",
      provider: "腾讯医疗AI实验室",
      difficulty: 5,
      tech_stack: ["Python", "PyTorch", "Flask"],
      status: "finished",
      team_size: 4,
      current_members: 4,
      description: "利用深度学习技术识别X光片中的异常病灶，提供辅助诊断建议。"
    }
  ];
  return projects;
};

export const getMockScrumBoard = (): ScrumBoard => {
  return {
    sprint_name: `Sprint ${getRandomInt(1, 5)}: 核心功能开发`,
    columns: {
      todo: [
        { id: "t_1", title: "设计数据库ER图", assignee: "张三", story_points: 3, priority: "high" },
        { id: "t_2", title: "编写API接口文档", assignee: "李四", story_points: 2, priority: "medium" }
      ],
      in_progress: [
        { id: "t_3", title: "用户登录注册接口", assignee: "王五", story_points: 5, priority: "high" },
        { id: "t_4", title: "搭建前端项目脚手架", assignee: "赵六", story_points: 3, priority: "high" }
      ],
      review: [
        { id: "t_5", title: "技术方案评审", assignee: "张三", story_points: 1, priority: "low" }
      ],
      done: [
        { id: "t_6", title: "需求分析说明书", assignee: "全体成员", story_points: 8, priority: "high" }
      ]
    }
  };
};

export interface Job {
  id: string;
  title: string;
  company: string;
  city: string;
  salary_min: number;
  salary_max: number;
  type: string;
  match_score: number;
  match_reason: string;
  tags: string[];
}

export interface InternshipReport {
  id: string;
  week_number: number;
  start_date: string;
  end_date: string;
  content_done: string;
  content_plan: string;
  content_problem: string;
  mood_score: number;
  status: 'draft' | 'submitted' | 'reviewed';
  mentor_comment?: string;
  teacher_comment?: string;
}

export const getMockJobs = (): Job[] => {
  return [
    {
      id: "job_001",
      title: "Java后端开发实习生",
      company: "字节跳动",
      city: "北京",
      salary_min: 250,
      salary_max: 400,
      type: "backend",
      match_score: 95,
      match_reason: "技术栈高度匹配，算法能力优秀",
      tags: ["大厂", "免费三餐", "转正机会"]
    },
    {
      id: "job_002",
      title: "前端开发实习生",
      company: "美团",
      city: "上海",
      salary_min: 200,
      salary_max: 300,
      type: "frontend",
      match_score: 88,
      match_reason: "React熟练，有相关项目经验",
      tags: ["核心业务", "技术氛围好"]
    },
    {
      id: "job_003",
      title: "全栈开发工程师(实习)",
      company: "某A轮创业公司",
      city: "杭州",
      salary_min: 150,
      salary_max: 250,
      type: "fullstack",
      match_score: 82,
      match_reason: "综合能力不错，适合创业团队",
      tags: ["弹性工作", "期权激励"]
    }
  ];
};

export const getMockReports = (): InternshipReport[] => {
  return [
    {
      id: "rpt_005",
      week_number: 5,
      start_date: "2024-05-20",
      end_date: "2024-05-26",
      content_done: "完成登录模块接口开发与单元测试；修复了上周遗留的Bug。",
      content_plan: "下周进行支付对接，阅读相关API文档。",
      content_problem: "支付回调的签名算法有点复杂，需要导师指导。",
      mood_score: 4,
      status: "reviewed",
      mentor_comment: "工作很扎实，代码规范性有提升，继续保持。",
      teacher_comment: "注意文档同步更新。"
    },
    {
      id: "rpt_006",
      week_number: 6,
      start_date: "2024-05-27",
      end_date: "2024-06-02",
      content_done: "支付接口联调完成50%。",
      content_plan: "完成剩余支付逻辑。",
      content_problem: "无",
      mood_score: 3,
      status: "submitted"
    }
  ];
};

export interface EvaluationResult {
  enterprise_score: number;
  school_score: number;
  bonus_score: number;
  total_score: number;
  comments: {
    id: string;
    source: 'enterprise' | 'school';
    author: string;
    content: string;
    date: string;
  }[];
}

export interface Certificate {
  id: string;
  title: string;
  issuer: string;
  issue_date: string;
  hash: string;
  status: 'valid' | 'revoked';
}

export interface Badge {
  id: string;
  name: string;
  description: string;
  category: 'tech' | 'soft' | 'special';
  unlocked_at: string;
}

export const getMockEvaluation = (): EvaluationResult => {
  const ent = getRandomInt(80, 95);
  const sch = getRandomInt(85, 98);
  const bonus = getRandomInt(5, 10);
  // Weights: 40% Ent, 30% Sch, 30% Bonus? No, usually total is weighted sum.
  // Let's assume the scores are raw 0-100, and we calculate a weighted total or just return a pre-calc one.
  // Doc says: Enterprise (40%), School (30%), Bonus (30%).
  const total = Math.round(ent * 0.4 + sch * 0.3 + (80 + bonus) * 0.3); // Fake calculation

  return {
    enterprise_score: ent,
    school_score: sch,
    bonus_score: 80 + bonus,
    total_score: total,
    comments: [
      {
        id: "c_1",
        source: "enterprise",
        author: "张导师 (字节跳动)",
        content: "该同学在实习期间表现优异，能够独立承担核心模块开发，代码质量高。",
        date: "2024-06-15"
      },
      {
        id: "c_2",
        source: "school",
        author: "李老师 (软件学院)",
        content: "实训课程作业完成度好，答辩逻辑清晰。",
        date: "2024-01-10"
      }
    ]
  };
};

export const getMockCertificates = (): Certificate[] => {
  return [
    {
      id: "cert_001",
      title: "Java全栈工程师实训结业证书",
      issuer: "智途教育 x 字节跳动",
      issue_date: "2023-12-30",
      hash: "0x7f83...a1b2",
      status: "valid"
    },
    {
      id: "cert_002",
      title: "优秀实习生荣誉证书",
      issuer: "美团点评",
      issue_date: "2024-06-20",
      hash: "0x9c21...d4e5",
      status: "valid"
    }
  ];
};

export const getMockBadges = (): Badge[] => {
  return [
    {
      id: "bdg_001",
      name: "Java全栈",
      description: "掌握Java后端与主流前端框架",
      category: "tech",
      unlocked_at: "2023-11-15"
    },
    {
      id: "bdg_002",
      name: "沟通达人",
      description: "在团队协作中表现出色的沟通能力",
      category: "soft",
      unlocked_at: "2023-12-01"
    },
    {
      id: "bdg_003",
      name: "Scrum Master",
      description: "熟练掌握敏捷开发流程",
      category: "tech",
      unlocked_at: "2024-01-20"
    },
    {
      id: "bdg_004",
      name: "Bug Hunter",
      description: "累计修复超过50个Bug",
      category: "special",
      unlocked_at: "2024-05-10"
    }
  ];
};

