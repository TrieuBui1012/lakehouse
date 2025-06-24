# ArgoCD Document
## Installation
1. Argo Helm Charts site: 
https://argoproj.github.io/argo-helm/
2. Add Argo Repo to helm:
```
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update
```
3. Install ArgoCD:
```
kubectl create namespace argocd
helm pull argo/argo-cd # if not pulling and configuring yet
# Configure values.yaml file
helm install -n argocd argocd argo-cd
```