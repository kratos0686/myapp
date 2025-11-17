# ML Models Directory

This directory is for TensorFlow Lite models (.tflite files).

Place your machine learning models here for use in the AR Room Scanner application.

## Supported Model Types
- Object detection models
- Segmentation models
- Depth estimation models
- Room layout prediction models

## Usage
Models placed in this directory will be automatically bound to the application when `mlModelBinding` is enabled in the app's build.gradle file.

Example:
```
app/src/main/ml/
├── object_detection.tflite
├── room_segmentation.tflite
└── depth_estimation.tflite
```

For each `.tflite` model file, Android Studio will generate a corresponding Kotlin class that can be used in your code.
