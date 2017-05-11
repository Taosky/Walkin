package science.zxc.walkin.util;

/**
 * AUTH: Taosky
 * TIME: 2017/5/11 0011:下午 10:57.
 * MAIL: t@firefoxcn.net
 * DESC:
 */

public class DirectionUtil {
    //根据角度判断方向
    public static String judgeDirection(final float direction) {
        String directionText;
        if (direction < -100 && direction > -170) directionText = "南偏西 ";
        else if (direction <= -80 && direction >= -100) directionText = "正西";
        else if (direction < -10 && direction > -80) directionText = "北偏西";
        else if (direction <= 10 && direction >= -10) directionText = "正北";
        else if (direction < 80 && direction > 10) directionText = "北偏东";
        else if (direction <= 100 && direction >= 80) directionText = "正东";
        else if (direction < 170 && direction > 100) directionText = "南偏东";
        else if (direction >= 170 || direction <= -170) directionText = "正南";
        else directionText = "未知";
        return directionText;
    }
}
