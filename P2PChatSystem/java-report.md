TECH STACK:
  TCP is used for direct message delivery between users because it ensures reliable, ordered, and error-checked delivery. This is crucial for preserving the integrity of chat messages, as users expect their messages to arrive in order and without loss or duplication.
  While TCP is reliable, its overhead makes it less efficient and more data hungry for broadcast-like scenarios, which is why it is not used for user discovery.
  
  UDP is used for broadcasting user presence as it allows fast and connectionless communication, making it ideal for continuously signaling online users in the local network.
  However, UDP does not guarantee message delivery and can suffer from a lot of packet loss. This is mitigated by periodic broadcasting and redundancy in the application logic.
  
  Swing provides the graphical user interface, offering a lightweight and extensible toolkit that integrates well with Java's core features. It is chosen for its portability and ease of use in creating a functional, event-driven chat interface.
  Swing's older design makes it less modern and slower compared to newer GUI frameworks like JavaFX, but its simplicity and wide adoption make it suitable for this project.
  
  ConcurrentHashMap is used to manage online users and their activity efficiently in a multithreaded environment. It provides thread-safe operations without the need for explicit synchronization.
  Its internal complexity may lead to slightly higher memory overhead compared to simpler maps, but its performance and thread-safety justify its use. Sacrifincing memory to be safer.
  
  Timers are used for periodic tasks like broadcasting user presence and checking for inactive users. They integrate seamlessly with the Swing framework and avoid blocking the UI thread.
  Timers might not be the best choice for high-frequency or complex tasks compared to scheduled executors, but their simplicity suffices for this project's needs.
  
  Java's built-in networking API provides straightforward abstractions for TCP and UDP communication, making it easy to implement the core functionalities of the chat system.

TESTING POLICY:
  
