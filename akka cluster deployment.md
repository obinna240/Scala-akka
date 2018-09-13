### The Basics of how it all works together
1. Assuming we have 4 nodes - RR, SWB, EMT and Task Worker
2. When we fire up the nodes they will seek to form a cluster
3. If we are using STATELESS NODES, our setup will be to predefine our seed nodes and provide a hostname for them
4. This means that when we start up the system, the seed nodes will be started first then the nodes we are trying to add will check to
see if the seed nodes are up and if they are, they will seek to join the seed nodes by sending a 'join' request.
5. Once that is done, then a cluster is formed.

### Using AKKA Cluster Bootstrap
1. When we use AKKA Cluster Bootstrap, the approach is completely different. AKKA Cluster Bootstrap serves 3 purposes
   - The self discovery of nodes in a cluster
   - Providing a mechanism for nodes to join discovered clusters
   - Safely forming a new cluster for discovered nodes
2. In Environments like Kubernetes, DNS records are utilized in self discovery simply because the clustered environment 
automatically manages the DNS records.

### Setting up AKKA Cluster Bootstrap and how it works


## What is Kubernetes?

## Akka Bootstrap



