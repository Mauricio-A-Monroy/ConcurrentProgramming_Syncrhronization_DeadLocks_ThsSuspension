
## Escuela Colombiana de Ingeniería
### Arquitecturas de Software – ARSW


#### Ejercicio – programación concurrente, condiciones de carrera y sincronización de hilos. EJERCICIO INDIVIDUAL O EN PAREJAS.

##### Parte I – Antes de terminar la clase.

Control de hilos con wait/notify. Productor/consumidor.

1. Revise el funcionamiento del programa y ejecútelo. Mientras esto ocurren, ejecute jVisualVM y revise el consumo de CPU del proceso correspondiente. A qué se debe este consumo?, cual es la clase responsable?


![image](https://github.com/user-attachments/assets/52a899b7-711b-407a-aca3-4330b4fc02a2)


	Al ejecutar el programa original, no vimos un gran consumo de CPU ni de memoria, así que decidimos dejar de dormir el hilo producer y obtuvimos este resultado:


![image](https://github.com/user-attachments/assets/ab2cecbf-fa6a-427a-8580-ef0057862f50)


Aunque el consumo de la CPU sigue siendo practicamente el mismo (entre 10% y 12%) lo que sí cambia drásticamente es la memoria, y podemos visualizar cuando el hilo producer añade números a la cola, y cuando el hilo consumer los está sacando, este consumo se debe a que el hilo consumer siempre está tratando de consumir de la cola.


2. Haga los ajustes necesarios para que la solución use más eficientemente la CPU, teniendo en cuenta que -por ahora- la producción es lenta y el consumo es rápido. Verifique con JVisualVM que el consumo de CPU se reduzca.


	![image](https://github.com/user-attachments/assets/202847ff-b401-4119-8ee9-13fd81bb1b6c)
	Para lograr esta reducción en el consumo de CPU, sincronizamos los hilos producer y consumer, los primeros van a esperar cuando la cola esté llena (la cola tenga un tamaño igual al del límite de stock) y los segundos van a esperar cuando la cola esté vacía, en ambos casos se debe sincronizar el acceso a la cola.

3. Haga que ahora el productor produzca muy rápido, y el consumidor consuma lento. Teniendo en cuenta que el productor conoce un límite de Stock (cuantos elementos debería tener, a lo sumo en la cola), haga que dicho límite se respete. Revise el API de la colección usada como cola para ver cómo garantizar que dicho límite no se supere. Verifique que, al poner un límite pequeño para el 'stock', no haya consumo alto de CPU ni errores.


	Para que el límite de Stock sea respetado agregamos un condicional en el run del hilo producer, ademas de dormir solo 0.005 ms para que produzca más rápido de lo que se consume:
```java
	@Override
    public void run() {
        while (true) {
            synchronized (queue) {
                while (this.queue.size() == stockLimit) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                dataSeed = dataSeed + rand.nextInt(100);
                System.out.println("Producer added " + dataSeed);
                queue.add(dataSeed);
                queue.notifyAll();
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
   ```

Para que los consumidores no consuman tan rápido, los dormimos siempre que consuman durante 0.01 ms:
```java
	@Override
    public void run() {
        while (true) {

            synchronized (queue){
                while(queue.size() == 0){
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                int elem = queue.poll();
                System.out.println("Consumer consumes " + elem);
                queue.notifyAll();

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
   ```


y el consumo de CPU es este:
![image](https://github.com/user-attachments/assets/f9041598-b69a-44e3-b30a-8f9fac5ee495)




##### Parte II. – Antes de terminar la clase.

Teniendo en cuenta los conceptos vistos de condición de carrera y sincronización, haga una nueva versión -más eficiente- del ejercicio anterior (el buscador de listas negras). En la versión actual, cada hilo se encarga de revisar el host en la totalidad del subconjunto de servidores que le corresponde, de manera que en conjunto se están explorando la totalidad de servidores. Teniendo esto en cuenta, haga que:

- La búsqueda distribuida se detenga (deje de buscar en las listas negras restantes) y retorne la respuesta apenas, en su conjunto, los hilos hayan detectado el número de ocurrencias requerido que determina si un host es confiable o no (_BLACK_LIST_ALARM_COUNT_).
- Lo anterior, garantizando que no se den condiciones de carrera.

##### Parte III. – Avance para el martes, antes de clase.

Sincronización y Dead-Locks.

![](http://files.explosm.net/comics/Matt/Bummed-forever.png)

1. Revise el programa “highlander-simulator”, dispuesto en el paquete edu.eci.arsw.highlandersim. Este es un juego en el que:

	* Se tienen N jugadores inmortales.
	* Cada jugador conoce a los N-1 jugador restantes.
	* Cada jugador, permanentemente, ataca a algún otro inmortal. El que primero ataca le resta M puntos de vida a su contrincante, y aumenta en esta misma cantidad sus propios puntos de vida.
	* El juego podría nunca tener un único ganador. Lo más probable es que al final sólo queden dos, peleando indefinidamente quitando y sumando puntos de vida.

2. Revise el código e identifique cómo se implemento la funcionalidad antes indicada. Dada la intención del juego, un invariante debería ser que la sumatoria de los puntos de vida de todos los jugadores siempre sea el mismo(claro está, en un instante de tiempo en el que no esté en proceso una operación de incremento/reducción de tiempo). Para este caso, para N jugadores, cual debería ser este valor?.


	Si cada jugador tiene una vida inicial de m, el invariante sería m*N. 

3. Ejecute la aplicación y verifique cómo funcionan las opción ‘pause and check’. Se cumple el invariante?.


	![image](https://github.com/user-attachments/assets/40b90bb1-7dcf-4f61-9d24-744b3bf0c442)
	![image](https://github.com/user-attachments/assets/7e117dc7-22c6-4247-9a6e-fa871d8f5343)

   La funcionalidad de la opción "pause and check" es mostrar cuanta vida tienen los jugadores actualmente y cuál es la sumatoria de la vida de los jugadores, como podemos ver en las capturas de pantalla, la sumatoria de la vida de los jugadores está aumentando, por lo que el invariante no se está cumpliendo.

 
4. Una primera hipótesis para que se presente la condición de carrera para dicha función (pause and check), es que el programa consulta la lista cuyos valores va a imprimir, a la vez que otros hilos modifican sus valores. Para corregir esto, haga lo que sea necesario para que efectivamente, antes de imprimir los resultados actuales, se pausen todos los demás hilos. Adicionalmente, implemente la opción ‘resume’.


	En la clase ControlFrame se agregaron estas lineas de código para hacer esperara a los hilos y reanudarlos.
	```java
    JButton btnPauseAndCheck = new JButton("Pause and check");
        btnPauseAndCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                /*
				 * COMPLETAR
                 */
                int sum = 0;
                for (Immortal im : immortals) {
                    im.setPaused(true);
                    sum += im.getHealth();
                }

                statisticsLabel.setText("<html>"+immortals.toString()+"<br>Health sum:"+ sum);

            }
        });
        toolBar.add(btnPauseAndCheck);
   ```
	```java
 	JButton btnResume = new JButton("Resume");

        btnResume.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                for (Immortal im : immortals) {
                    im.setPaused(false);
                }

            }
        });
   ```


 	Ela clase Immortal, se añadió un atributo para saber si el hilo debía pausarse o no, un método para cambiar el valor de este atributo y la verificación del valor del atributo en el método run para para el hilo o dejar que se siga ejecutando.
	```java
 	private boolean isPaused = false;
 
 	...
 
 	public void run() {

        while (true) {

            synchronized (this) {
                while (isPaused) {
                    try {
                        wait();  // Pausa el hilo si `paused` es true
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
 
 		...
 
        }

    }

 	...

	public void setPaused(boolean isPaused) {
        synchronized (this) {
            this.isPaused = isPaused;
            if (!isPaused) {
                notifyAll();  // Notifica a todos los hilos que están esperando
            }
        }
    }
   ```
	

 
5. Verifique nuevamente el funcionamiento (haga clic muchas veces en el botón). Se cumple o no el invariante?.


 	Aun no se cumplé el invariante, la suma de la vida de los jugadores sigue aumentando a medida que pasa el tiempo.


6. Identifique posibles regiones críticas en lo que respecta a la pelea de los inmortales. Implemente una estrategia de bloqueo que evite las condiciones de carrera. Recuerde que si usted requiere usar dos o más ‘locks’ simultáneamente, puede usar bloques sincronizados anidados:

   ```java
   synchronized(locka){
       synchronized(lockb){
           …
       }
   }
   ```

   La región crítica se encuentra en el método fight() de la clase immortal, ya que aquí podemos tener problemas cuando los jugadores usan el getter y setter para cambiar el valor de su salud y el de sus rivales.

7. Tras implementar su estrategia, ponga a correr su programa, y ponga atención a si éste se llega a detener. Si es así, use los programas jps y jstack para identificar por qué el programa se detuvo.

8. Plantee una estrategia para corregir el problema antes identificado (puede revisar de nuevo las páginas 206 y 207 de _Java Concurrency in Practice_).

9. Una vez corregido el problema, rectifique que el programa siga funcionando de manera consistente cuando se ejecutan 100, 1000 o 10000 inmortales. Si en estos casos grandes se empieza a incumplir de nuevo el invariante, debe analizar lo realizado en el paso 4.

10. Un elemento molesto para la simulación es que en cierto punto de la misma hay pocos 'inmortales' vivos realizando peleas fallidas con 'inmortales' ya muertos. Es necesario ir suprimiendo los inmortales muertos de la simulación a medida que van muriendo. Para esto:
	* Analizando el esquema de funcionamiento de la simulación, esto podría crear una condición de carrera? Implemente la funcionalidad, ejecute la simulación y observe qué problema se presenta cuando hay muchos 'inmortales' en la misma. Escriba sus conclusiones al respecto en el archivo RESPUESTAS.txt.
	* Corrija el problema anterior __SIN hacer uso de sincronización__, pues volver secuencial el acceso a la lista compartida de inmortales haría extremadamente lenta la simulación.

11. Para finalizar, implemente la opción STOP.

<!--
### Criterios de evaluación

1. Parte I.
	* Funcional: La simulación de producción/consumidor se ejecuta eficientemente (sin esperas activas).

2. Parte II. (Retomando el laboratorio 1)
	* Se modificó el ejercicio anterior para que los hilos llevaran conjuntamente (compartido) el número de ocurrencias encontradas, y se finalizaran y retornaran el valor en cuanto dicho número de ocurrencias fuera el esperado.
	* Se garantiza que no se den condiciones de carrera modificando el acceso concurrente al valor compartido (número de ocurrencias).


2. Parte III.
	* Diseño:
		- Coordinación de hilos:
			* Para pausar la pelea, se debe lograr que el hilo principal induzca a los otros a que se suspendan a sí mismos. Se debe también tener en cuenta que sólo se debe mostrar la sumatoria de los puntos de vida cuando se asegure que todos los hilos han sido suspendidos.
			* Si para lo anterior se recorre a todo el conjunto de hilos para ver su estado, se evalúa como R, por ser muy ineficiente.
			* Si para lo anterior los hilos manipulan un contador concurrentemente, pero lo hacen sin tener en cuenta que el incremento de un contador no es una operación atómica -es decir, que puede causar una condición de carrera- , se evalúa como R. En este caso se debería sincronizar el acceso, o usar tipos atómicos como AtomicInteger).

		- Consistencia ante la concurrencia
			* Para garantizar la consistencia en la pelea entre dos inmortales, se debe sincronizar el acceso a cualquier otra pelea que involucre a uno, al otro, o a los dos simultáneamente:
			* En los bloques anidados de sincronización requeridos para lo anterior, se debe garantizar que si los mismos locks son usados en dos peleas simultánemante, éstos será usados en el mismo orden para evitar deadlocks.
			* En caso de sincronizar el acceso a la pelea con un LOCK común, se evaluará como M, pues esto hace secuencial todas las peleas.
			* La lista de inmortales debe reducirse en la medida que éstos mueran, pero esta operación debe realizarse SIN sincronización, sino haciendo uso de una colección concurrente (no bloqueante).

	

	* Funcionalidad:
		* Se cumple con el invariante al usar la aplicación con 10, 100 o 1000 hilos.
		* La aplicación puede reanudar y finalizar(stop) su ejecución.
		
		-->

<a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a><br />Este contenido hace parte del curso Arquitecturas de Software del programa de Ingeniería de Sistemas de la Escuela Colombiana de Ingeniería, y está licenciado como <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/">Creative Commons Attribution-NonCommercial 4.0 International License</a>.
