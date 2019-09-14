# Sharing Security Incident Knowledge in Cyber-Physical Systems

We propose two automated techniques that can be used to facilitate the sharing of security incident knowledge across different cyber-physical systems.

Here, we provide the implementation of the two techniques: incident pattern **extraction** and **instantiation**. 
We also provide examples to demonstrate the use of both techniques.

### Incident Pattern Extraction Technique

The technique aims at generating an incident pattern from an incident instance that occurred in a specific cyber-physical system, in particular, smart buildings.

The implementation can be found under the package: ie.lero.spare.pattern_extraction

To demonstrate the technique, you can download the executable jar for the project and execute the provided example (executable_jar/scenario1_extraction). Alternatively, you can download the project and build it then execute the example.

to execute the technique: 
```
>> java -jar techniques.jar 

// type 1 to execute the technique
>> 1 
```


### Incident Pattern Instantiation Technique

The technique aims at identfiying all traces (i.e. sequences of actions) in a system that satisfy a given incident pattern.

The implementation can be found under the package: ie.lero.spare.pattern_instantiation

To demonstrate the technique, you can download the executable jar for the project and execute the provided example (executable_jar/scenario1_instantiation). Alternatively, you can download the project and build it then execute the example .

 
to execute the technique: 
```
>>java -jar techniques.jar 

// type 2 to execute the technique
>>2
```
