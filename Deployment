# Deployments

Helm streamlines installing and managing Kubernetes applications
A Kubernetes Pod is a group of one or more Containers, tied together for the purposes of administration and networking.  
A Kubernetes Deployment checks on the health of your Pod and restarts the Pod’s Container if it terminates. 
Deployments are the recommended way to manage the creation and scaling of Pods.
Use the `kubectl run` command to create a Deployment that manages a Pod. 
The Pod runs a Container based on your `hello-node:v1` Docker image. Set the `--image-pull-policy` flag to `Never` to 
always use the local image, rather than pulling it from your Docker registry (since you haven’t pushed it there):
```
kubectl run hello-node --image=hello-node:v1 --port=8080 --image-pull-policy=Never
```
To view the deployment we can do
```
kubectl get deployments
```
To view the pods
```
kubectl get pods
```
To view the cluster events
```
kubectl get events
```
to view the configuration
```
kubectl config view
```
