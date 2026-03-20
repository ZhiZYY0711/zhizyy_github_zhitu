import { useRef, useState, useEffect, type FormEvent } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useAuth } from '../auth/context';
import { ROLE_LABELS, ROLE_ROUTES, type UserRole } from '../auth/types';

const LoginPage = () => {
  const { login, isAuthenticated, getDashboardPath } = useAuth();
  const navigate = useNavigate();  const usernameRef = useRef<HTMLInputElement>(null);

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<UserRole>('student');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    usernameRef.current?.focus();
  }, []);

  // Already logged in → redirect to their dashboard
  if (isAuthenticated) {
    return <Navigate to={getDashboardPath()} replace />;
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    // Simulate async login
    await new Promise(r => setTimeout(r, 600));

    const result = await login({ username, password, role });
    setLoading(false);

    if (!result.success) {
      setError(result.error ?? '登录失败');
      return;
    }

    // Use role directly — getDashboardPath() reads stale session state at this point
    const destination = ROLE_ROUTES[role];
    navigate(destination, { replace: true });
  };

  const roleOptions = Object.entries(ROLE_LABELS) as [UserRole, string][];

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-100 to-slate-200 flex items-center justify-center p-4">
      <div className="w-full max-w-md space-y-6">
        {/* Logo / Title */}
        <div className="text-center space-y-2">
          <div className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-blue-600 shadow-lg">
            <span className="text-2xl font-bold text-white">智</span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight">智途平台</h1>
          <p className="text-sm text-muted-foreground">产教融合一体化管理系统</p>
        </div>

        <Card className="shadow-lg">
          <CardHeader className="pb-4">
            <CardTitle className="text-lg">登录</CardTitle>
            <CardDescription>请选择您的角色并输入账号信息</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Role selector */}
              <div className="space-y-1.5">
                <Label htmlFor="role">登录角色</Label>
                <Select value={role} onValueChange={v => setRole(v as UserRole)}>
                  <SelectTrigger id="role">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {roleOptions.map(([value, label]) => (
                      <SelectItem key={value} value={value}>{label}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {/* Username */}
              <div className="space-y-1.5">
                <Label htmlFor="username">用户名</Label>
                <Input
                  id="username"
                  ref={usernameRef}
                  placeholder="请输入用户名"
                  value={username}
                  onChange={e => setUsername(e.target.value)}
                  autoComplete="username"
                />
              </div>

              {/* Password */}
              <div className="space-y-1.5">
                <Label htmlFor="password">密码</Label>
                <Input
                  id="password"
                  type="password"
                  placeholder="请输入密码"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  autoComplete="current-password"
                />
              </div>

              {/* Error */}
              {error && (
                <p className="text-sm text-red-500 bg-red-50 border border-red-200 rounded-md px-3 py-2">
                  {error}
                </p>
              )}

              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? '登录中...' : '登录'}
              </Button>
            </form>

            <p className="mt-4 text-center text-xs text-muted-foreground">
              开发模式：输入任意用户名和密码即可登录
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default LoginPage;
