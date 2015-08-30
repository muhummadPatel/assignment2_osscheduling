##Assignment 2 - OS Scheduling

Author : Muhummad Yunus Patel
Student# : PTLMUH006
Date : 25-March-2015

_NOTE_: Should you require any clarification, please feel free to contact me 
           at muhummad.patel@gmail.com. Thank you. :)

###Description:
This assignment involved trying to model the behaviour of an operating system 
kernel from the perspective of process scheduling. A basic framework of 
component interfaces was provided to guide the design and implementation of this
 simulation. My design is based on the framework provided and is comprised of 
 the following 4 components:
 * The Simulation driver:
  * This can be thought of as the "motherboard" of this simulation as it 
     connects and coordinates all of the components required for proper
     functioning.
  * sets up the simulation by loading the config files, adding initial events 
     to the eventQueue, and initialising all components.
  * Manages the main simulation loop. The main simulation loop executes any 
     event that is scheduled for before or at the current system time, and then 
     executes the cpu until the next event is due to occur.
  * Manages the eventQueue. The EventQueue is a queue of events that are 
     waiting to be processed. These events are load program evts, timout evts, 
     or wakeup evts.
  * Manages processing of events. Events will only be processed at the time 
     they have been scheduled for. They are not allowed to execute before time. 
     This is accomplished by consulting the SystemTimer instance.
    
    >The Kernel:___
    +This is the simulation of a kernel in an operating system.
    +It schedules aspects of kernel behaviour relating to scheduling. Since the 
     focus of this simulation is the scheduling aspect of operating systems, 
     this is really all we need from the kernel.
    +Manages the readyQueue. The readyQueue is a queue of processes waiting to 
     be processed. The processes are represented by ProcessControlBlock objects.
     The readyQueue is used for scheduling.
    +Manages the deviceQueue. Each device that is part of the simulation is 
     stored in the deviceQueue. These devices each have their own queues of 
     processes waiting to use them. The deviceQueue is used to access devices 
     and their queues of waiting processes.
    +As events are handled and time proceeds, processes are moved between queues
     and onto/off of the CPU by the kernel. Just like in a real OS.
    
    >The CPU:___
    +The CPU simulates the processing of program instructions for a given amount
     of time. The cpu is asked to execute its current process by the Simulation 
     driver. The CPU is called to execute between events by the simulation 
     driver.
    +It also provides a context switching method that is used by the kernel to
     swap processes onto/off of the CPU.
    
    >The System Timer:___
    +Records the current System time. This is the overall time that the 
     simulation is currently at.
    +Records the time spent in user space(userTime). This is updated by the CPU
     to reflect the time spent executing user processes.
    +Records the time spent in kernel space(kernelTime). This is updated by the 
     kernel to reflect the time spent executing kernel tasks.
    +Outputs required statistics (including idleTime and number of context 
     switches).



Compiling and Running:
A makefile has been provided in the src folder. See below for compile and run 
steps.
>To compile, navigate to the src folder and type make.

>To run, type make run_test<x> (x is the test case to run (x is 1, 2, 3, or 4)).
 eg. make run_test1
 
>To clean up, type make clean.

NOTE: to run your own tests, copy the test folder (for example of test folder, 
see TestOne in the src folder) into the src folder and compile (make) and then 
type java Simulator <yourFolder/configFileName.cfg> <slice length> <dispatch overhead>.