& minikube -p minikube docker-env --shell powershell | Invoke-Expression

cd backend\data-provider
docker build -t data-provider:latest .

cd ../..
kubectl apply -f backend\data-provider\k8s\
kubectl delete pod -l app=data-provider -n stock-management

kubectl get pods -n stock-management -l app=data-provider
kubectl logs <pod-name> -n stock-management