# Sharing Security Incident Knowledge in Cyber-Physical Systems

We propose two automated techniques that can be used to facilitate the sharing of security incident knowledge across different cyber-physical systems.

Here, we provide the implementation of the two techniques: incident pattern **extraction** and **instantiation**. 
We also provide examples to demonstrate the use of both techniques.

### Incident Pattern Extraction Technique

The technique aims at generating an incident pattern from an incident instance that occurred in a specific cyber-physical system, in particular, smart buildings.

The implementation can be found under the package: ie.lero.spare.pattern_extraction

To demonstrate the technique, you can download the executable jar for the project and execute the provided example (executable_jar/scenario1_extraction). Alternatively, you can download the project and build it then execute the example.

Under *executable_jar/scenario1_extraction*, the Demo for executing the extraction technique contains the following data:

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

To demonstrate the technique, you can download the executable jar for the project and execute the provided example (executable_jar/scenario2_instantiation). Alternatively, you can download the project and build it then execute the example .

Under *executable_jar/scenario2_instantiation*, the Demo for executing the instantiation technique contains the following data:

>- Incident pattern model file (*incidentPattern.cpi*)

>- System model file (*RC2.cps*), and Bigraphical Reactive Representation (BRS) of the system (*RC2.big*)

>- RC2 Labelled Transition System (LTS). The LTS has 2005 states. We provide a small LTS for size considerations and just for demoing. If larger sizes are required please don't hesitate to contact us (faeq.alrimawi@lero.ie).

to execute the technique: 
```
>> java -jar techniques.jar 

// type 2 to execute the technique
>> 2
```

[1]:../../tree/master/executable_jar/DemoData/scenario1_extraction/incidentInstance.cpi
[2]:../../tree/master/executable_jar/DemoData/scenario1_extraction/RC1.cps
[1]:../../tree/master/executable_jar/DemoData/scenario1_extraction/activityPatterns
