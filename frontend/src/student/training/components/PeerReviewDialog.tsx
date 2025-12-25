import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

interface PeerReviewDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const PeerReviewDialog: React.FC<PeerReviewDialogProps> = ({ open, onOpenChange }) => {
  const [selectedMember, setSelectedMember] = useState<string>("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!selectedMember) return;
    setLoading(true);
    // Mock submit
    await new Promise(resolve => setTimeout(resolve, 1000));
    setLoading(false);
    onOpenChange(false);
    alert("评价提交成功！");
    setSelectedMember("");
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>360度成员互评</DialogTitle>
          <DialogDescription>
            请客观评价项目组成员的表现。评价结果将作为实训成绩的重要参考。
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-4">
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="member" className="text-right">
              评价对象
            </Label>
            <Select value={selectedMember} onValueChange={setSelectedMember}>
              <SelectTrigger className="col-span-3">
                <SelectValue placeholder="选择成员" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="zhangsan">张三 (后端开发)</SelectItem>
                <SelectItem value="lisi">李四 (前端开发)</SelectItem>
                <SelectItem value="wangwu">王五 (测试工程师)</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="contribution" className="text-right">
              贡献度
            </Label>
            <Input id="contribution" type="number" min="1" max="10" defaultValue="8" className="col-span-3" />
          </div>
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="cooperation" className="text-right">
              配合度
            </Label>
            <Input id="cooperation" type="number" min="1" max="10" defaultValue="9" className="col-span-3" />
          </div>
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="skill" className="text-right">
              技术水平
            </Label>
            <Input id="skill" type="number" min="1" max="10" defaultValue="8" className="col-span-3" />
          </div>
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="comment" className="text-right">
              评语
            </Label>
            <Input id="comment" placeholder="请输入简短评语..." className="col-span-3" />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>取消</Button>
          <Button onClick={handleSubmit} disabled={loading || !selectedMember}>
            {loading ? "提交中..." : "提交评价"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default PeerReviewDialog;
