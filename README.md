# Sharing Security Incident Knowledge in Cyber-Physical Systems

We propose two automated techniques that can be used to facilitate the sharing of security incident knowledge across different cyber-physical systems.

We provide the implementation of the two techniques: incident pattern **extraction** and **instantiation**. 
We also give [Demo Data][4] to allow one to demonstrate the use of both techniques.

### Incident Pattern Extraction Technique

The technique aims at generating an incident pattern from an incident instance that occurred in a specific cyber-physical system, in particular, smart buildings.

To demonstrate the technique, you can download the [executable jar][4] for the project and execute it. Alternatively, you can download the project and build it then execute the example.

Under [Scenario 1][5], the Demo for executing the extraction technique contains the following data:

>- Incident instance model file ([incidentInstance.cpi][1])

>- System model file ([RC1.cps][2])

>- Activity pattern files under the folder [activityPatterns][3]

to execute the technique: 

```
>> java -jar techniques.jar 

// type 1 to execute the technique
>> 1 
```


### Incident Pattern Instantiation Technique

The technique aims at identfiying all traces (i.e. sequences of actions) in a system that satisfy a given incident pattern.

The implementation can be found under the package: ie.lero.spare.pattern_instantiation

To demonstrate the technique, you can download the [executable jar][4] for the project and execute the provided example. Alternatively, you can download the project and build it then execute the example .

Under [Scenario 2][6], the Demo for executing the instantiation technique contains the following data:

>- Incident pattern model file ([incidentPattern.cpi][7])

>- System model file ([RC2.cps][8]), and Bigraphical Reactive Representation (BRS) of the system ([RC2.big][9])

>- RC2 Labelled Transition System ([LTS][10]). The LTS has 2005 states. We provide a small LTS for size considerations and just for demoing. If larger sizes are required please don't hesitate to contact us (faeq.alrimawi@lero.ie).

to execute the technique: 
```
>> java -jar techniques.jar 

// type 2 to execute the technique
>> 2
```
[4]:../../tree/master/executable_jar
[5]:../../tree/master/executable_jar/DemoData/scenario1_extraction/
[1]:../../tree/master/executable_jar/DemoData/scenario1_extraction/incidentInstance.cpi
[2]:../../tree/master/executable_jar/DemoData/scenario1_extraction/RC1.cps
[3]:../../tree/master/executable_jar/DemoData/scenario1_extraction/activityPatterns

[6]:../../tree/master/executable_jar/DemoData/scenario2_instantiation/
[7]:../../tree/master/executable_jar/DemoData/scenario2_instantiation/incidentPattern.cpi
[8]:../../tree/master/executable_jar/DemoData/scenario2_instantiation/RC2.cps
[9]:../../tree/master/executable_jar/DemoData/scenario2_instantiation/RC2.big
[10]:../../tree/master/executable_jar/DemoData/scenario2_instantiation/RC2/
