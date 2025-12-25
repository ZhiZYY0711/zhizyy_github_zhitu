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
