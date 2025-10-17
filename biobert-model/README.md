---
language: 
  - en
thumbnail: "https://fly.health"
tags:
- FHIR 
- Healthcare
- Questions
license: mit
datasets:
- fhirfly/medicalquestions
metrics:
- Validation Accuracy .16
---
# 🤗 Model Card: Medical Questions Classifier

## Model Details

- Model name: Medical Questions Classifier
- Model type: BERT-based single label classification network
- Architecture: BERT (Bidirectional Encoder Representations from Transformers)
- Training objective: Binary classification (medical question or non-medical question)
- Training data: 16,000 medical and non-medical questions
- Training time: [Training duration]

## Intended Use

The Medical Questions Classifier is designed to classify whether a given text is a medical question or a non-medical question. It can be used to filter and categorize user queries, enabling more efficient routing of questions to appropriate medical professionals or providing targeted responses.

This model is not intended for diagnosing medical conditions or providing medical advice. It should be used as a tool to assist in the organization and routing of inquiries rather than replacing professional medical expertise.

## Limitations and Ethical Considerations

- **Domain limitation**: The model has been trained on a diverse set of 16,000 medical and non-medical questions. However, it may not perform optimally on highly specialized medical topics or rare medical conditions not well represented in the training data.
- **Bias and fairness**: The training data used to train this model may have inherent biases, which can lead to biased predictions. Care should be taken to evaluate and mitigate potential biases during model deployment.
- **Uncertain predictions**: The model's predictions may not always be accurate, and it's important to exercise caution when relying solely on its output. Human review and expert opinion should be sought in critical or sensitive situations.
- **Data privacy**: As with any language model, it's crucial to handle user data responsibly and ensure compliance with relevant data protection regulations. Care should be taken to avoid sharing sensitive or personally identifiable information.

## Evaluation

The Medical Questions Classifier has been evaluated using standard metrics such as accuracy, precision, recall, and F1 score. On a held-out test set, it achieved an accuracy of [accuracy score] and an F1 score of [F1 score]. However, the performance may vary based on the specific use case and the distribution of questions in real-world applications.

## Training Data

The training data consists of 25,000+ questions sourced from various medical and non-medical domains. Efforts were made to ensure diversity in question types, covering a wide range of medical conditions, symptoms, treatments, and general non-medical topics.

The training data was manually labeled by domain experts, who classified each question as either "medical" or "non-medical" based on the context and content. The labeled data was then used to train the Medical Questions Classifier using a BERT-based architecture.

## Ethical Considerations

- **Data privacy and security**: It is crucial to handle user data with utmost care, ensuring compliance with applicable data privacy regulations and guidelines. Any data collected during the usage of this model should be handled securely and used responsibly.
- **Transparency**: This model card aims to provide transparency regarding the model's purpose, limitations, and potential biases. Users should have access to information about how the model works and its intended use.
- **Bias and fairness**: Biases present in the training data may be reflected in the model's predictions. It is important to evaluate the model's behavior, identify potential biases, and take steps to mitigate them during deployment.
- **Supervision and monitoring**: Continuous monitoring of the model's performance and user feedback is essential to ensure its effectiveness, accuracy, and fairness. Regular updates and improvements should be made based on real-world feedback and evolving best practices.

## Conclusion

The Medical Questions Classifier is a BERT-based model trained on 16,000 medical and non-medical questions. It can help identify whether a given text is a medical question or a non-medical question, enabling efficient triaging and routing of inquiries. However, it has limitations and should not be used as a substitute for professional medical expertise. Care should be taken to address potential biases and ensure responsible data handling when using this model.
