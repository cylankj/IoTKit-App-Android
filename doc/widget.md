#### 此文件跟踪了新增加的`控件`,并且添加使用说明

*   **ImageViewTip**
>   自定义了一个带红色圆圈的`imageView`,红色圆圈位置可配置

```java
    <declare-styleable name="DotThemes">
        <!--位置,0-7,顺时针的8个位置-->
        <attr name="position" format="integer" />
        <!--忽略padding属性,true:忽略-->
        <attr name="ignore" format="boolean" />
        <!--红点的半径-->
        <attr name="Dradius" format="dimension" />
        <attr name="margin" format="dimension" />
        <!--是否显示红点-->
        <attr name="showDot" format="boolean" />
        <!--用图标代替红点.-->
        <attr name="tipDrawable" format="reference" />
        <!--是否显示,边框背景-->
        <attr name="enableBorder" format="boolean" />
        <attr name="borderColor" format="color" />
        <attr name="borderWidth" format="dimension" />
    </declare-styleable>
```