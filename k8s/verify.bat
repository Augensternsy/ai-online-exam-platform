@echo off
echo ========================================
echo 考试系统 K8s 验证脚本
echo ========================================

echo.
echo [1/5] 检查 K8s 集群状态...
kubectl cluster-info

echo.
echo [2/5] 部署应用到 K8s...
kubectl apply -f k8s/

echo.
echo [3/5] 等待所有 Pod 就绪...
kubectl wait --for=condition=ready pod --all -n xzs-exam --timeout=300s

echo.
echo [4/5] 查看服务状态...
kubectl get all -n xzs-exam

echo.
echo [5/5] 验证后端服务...
kubectl run test-pod --image=busybox --rm -it --restart=Never -n xzs-exam -- wget -qO- http://xzs-backend-service:8000/api/student/dashboard/index

echo.
echo ========================================
echo 验证完成！
echo ========================================
pause
