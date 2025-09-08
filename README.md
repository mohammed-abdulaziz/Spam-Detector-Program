# Spam Detector

A Java-based spam detection program that uses a probability-driven algorithm (Naive Bayes–style with Laplace smoothing) to classify emails as either spam or ham (legitimate). The program allows users to upload datasets with spam and ham emails, train a classification model, and evaluate the overall performance using metrics such as accuracy, precision, recall, and F1-Score. This application features a Java Swing GUI for dataset selection, classification results, and performance metrics. The classification results are color-coded: ham emails appear in green, spam emails appear in red, and any misclassified emails are highlighted in yellow.


## How to Run

1. **Prerequisites:**
   - Ensure Java is installed on your system.
   - Ensure Git is installed for cloning the repository.


2. **Clone the Repository:**

   - `git clone <repo_link>`


3. **Change Directory:**
   
   - `cd <repository_name>/src/main`


4. **Compile the Application:**
  
   - `javac -d ../bin -sourcepath . SpamDetectorGUI.java`


5. **Run the Application:**
   
   - `java -cp ../bin SpamDetectorGUI`


6. **Dataset Structure:** 
When prompted, select a folder that contains the following structure:
```
train/
   spam/
   ham/
test/
   spam/
   ham/
```

## Screenshot:

![alt text](image-2.png)

## Credits

This project was developed by:

- Mohammed Abdulaziz
- Fardin Alam
- Muhammad Hassan