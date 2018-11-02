# router-sim
A basic local simulation of packet routing made for an assignment in the 2nd Year of my CS course

The program uses local ports to simulate a network of routers and two endpoints. 
The network is described by files written according to the syntax set out in `configtemplate`,
and the file `config` contains a newline separated list of filenames that each describe a node.
The `e*` and `r*` files describe an example network, with `e*` nodes being endpoints and 
`r*` nodes being intermediate routers. The program expects this convention to assign the
data correctly, and currently can only work with two enpoints, titled `e1` and `e2`.

The program sends output about the actions in the simulation to `wrapperout.txt`, 
with the one currently in the repository containing an example output.

`src/Wrapper.java` should serve as the main class of the project.
