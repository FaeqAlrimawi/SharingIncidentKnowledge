# Sharing Security Incident Knowledge in Cyber-Physical Systems

We propose two automated techniques that can be used to facilitate the sharing of security incident knowledge across different cyber-physical systems.

We provide the implementation of the two techniques: incident pattern **extraction** and **instantiation**. 
We also give [Demo Data][4] to allow one to use and test both techniques.

### Incident Pattern Extraction Technique

The technique aims at generating an incident pattern from an incident instance that occurred in a specific cyber-physical system, in particular, smart buildings.

To demonstrate the technique, you can download a demo (**demo.zip**) from [here][4]. The demo contains an exectable jar of both techniques (**techniques.jar**) and data (under **DemoData** folder). Unpack the demo and then run the jar file. Alternatively, you can download the project, build it, and then execute/modify the example from class [ScenarioExecutor.java][11].

The data for executing the extraction technique are:

>- Incident instance model file (**incidentInstance.cpi**)

>- System model file (**RC1.cps**)

>- Activity pattern files under the folder **activityPatterns**

To run the demo for the extraction technique, do the following: 

```console
foo@bar:~$ java -jar techniques.jar

...
Enter 1 or 2 to execute incident pattern extraction (1) or incident pattern instantiation (2)
1
```

The output of this technique is an incident model (**incidentInstance_abstract.cpi**), which is an abstract representation of the original incident instance. The output is stored in the same place as the incident instance file, i.e. under **scenario1_extraction** folder. The activities carried out by the technique are logged in **log** folder, which, if not created, will be created at the texecution of the technique.

### Incident Pattern Instantiation Technique

The technique aims at identfiying all traces (i.e. sequences of actions) in a system that satisfy a given incident pattern.

The implementation can be found under the package: ie.lero.spare.pattern_instantiation

To demonstrate the technique, you can download a demo (**demo.zip**) from [here][4]. The demo contains an exectable jar of both techniques (**techniques.jar**) and data (under **DemoData** folder). Unpack the demo and then run the jar file. Alternatively, you can download the project, build it, and then execute/modify the example from class [ScenarioExecutor.java][11].

The data for executing the isntantiation technique are:

>- Incident pattern model file (**incidentPattern.cpi**)

>- System model file (**RC2.cps**), and Bigraphical Reactive Representation (BRS) of the system (**RC2.big**)

>- Labelled Transition System (**LTS**). The LTS has 2005 states. We provide a small LTS for size considerations and just for demoing. If you want to experiment with systems of larger sizes please contact us (faeq.alrimawi@lero.ie).

To run the demo for the instantiation technique, do the following:  

```console
foo@bar:~$ java -jar techniques.jar

...
Enter 1 or 2 to execute incident pattern extraction (1) or incident pattern instantiation (2)
2
```

The output of the technique is the set of system traces (i.e. sequences of actions) that ssatisfy the given incident pattern. The identified traces are stored in a JSON file in folder **output** (which is created if it does not exist) under **scenario2_instantiation** folder. The activities carried out by the technique are logged in **log** folder, which, if not created, will be created at the texecution of the technique.

Output sample:

```json
    "potential_incident_instances": {
        "instances_count": 2,
        "instances": [
            {
                "instance_id": 0,
                "transitions": [
                    {
                        "action": "EnterRoom",
                        "source": 1,
                        "target": 64
                    },
                    {
                        "action": "ConnectBusDevice",
                        "source": 64,
                        "target": 271
                    },
                    {
                        "action": "CollectData",
                        "source": 271,
                        "target": 937
                    }
                ]
            },
            {
                "instance_id": 1,
                "transitions": [
                    {
                        "action": "EnterRoom",
                        "source": 1,
                        "target": 63
                    },
                    {
                        "action": "ConnectBusDevice",
                        "source": 63,
                        "target": 274
                    },
                    {
                        "action": "CollectData",
                        "source": 274,
                        "target": 946
                    }
                ]
            }
            ]}
```
The above excerpt shows the number of generated traces (i.e. **instances_count:2**), and traces information. Each trace has the following: 
>- **instance_id**, a unique identifier for the trace
>- **transitions**, the sequence of states and actions that satify the incident pattern activities. A transition consists of: 
>>- **action**, the system action that was invoked
>>- **source**, the source system state, which represents the system state *before* the action was invoked
>>- **target**, the target system state, which represents the system state *after* the action was invoked


[4]:../../tree/master/demo
[5]:../../tree/master/executable_jar/DemoData/scenario1_extraction/
[1]:../../tree/master/executable_jar/DemoData/scenario1_extraction/incidentInstance.cpi
[2]:../../tree/master/executable_jar/DemoData/scenario1_extraction/RC1.cps
[3]:../../tree/master/executable_jar/DemoData/scenario1_extraction/activityPatterns

[6]:../../tree/master/executable_jar/DemoData/scenario2_instantiation/
[7]:../../tree/master/executable_jar/DemoData/scenario2_instantiation/incidentPattern.cpi
[8]:../../tree/master/executable_jar/DemoData/scenario2_instantiation/RC2.cps
[9]:../../tree/master/executable_jar/DemoData/scenario2_instantiation/RC2.big
[10]:../../tree/master/executable_jar/DemoData/scenario2_instantiation/RC2/

[11]:../../tree/master/src/ie/lero/spare/main/ScenarioExecutor.java
