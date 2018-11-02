# StepProgressView
An Android library written in kotlin to display steps in a progress bar

## Setup

```
implementation 'com.params.progressview:step-progress:1.0.2'
```


## Usage
![Loading!!](https://github.com/params-ing/StepProgressView/blob/master/screenshots/step-progress-basic.png)
```
    <params.com.stepprogressview.StepProgressView
        android:layout_width="300dp"
        app:markers="10,60,120"
        app:totalProgress="130"
        app:currentProgress="40"
        app:markerWidth="3dp"
        app:textMargin="5dp"
        app:textSize="15sp"
        app:markerColor="@android:color/white"
        app:progressColor="@color/colorPrimaryDark" />
```

### TODO
* Status overlap in case the text labels are too close <br>
