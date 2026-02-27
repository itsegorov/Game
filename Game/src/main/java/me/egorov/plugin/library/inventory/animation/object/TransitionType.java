package me.egorov.plugin.library.inventory.animation.object;

public enum TransitionType {

    NONE("none", "Мгновенно"),
    FADE("fade", "Затухание"),
    SLIDE_LEFT("slide_left", "Слайд влево"),
    SLIDE_RIGHT("slide_right", "Слайд вправо"),
    SLIDE_UP("slide_up", "Слайд вверх"),
    SLIDE_DOWN("slide_down", "Слайд вниз"),
    ZOOM_IN("zoom_in", "Увеличение"),
    ZOOM_OUT("zoom_out", "Уменьшение"),
    ROTATE("rotate", "Вращение"),
    BLIND("blind", "Шторки"),
    CHESS("chess", "Шахматы"),
    WAVE("wave", "Волна"),
    GLASS("glass", "Стекло"),
    FALL("fall", "Падение"),
    RAINBOW("rainbow", "Радуга"),
    MATRIX("matrix", "Матрица"),
    FIREWORK("firework", "Фейерверк"),
    FLIP("flip", "Переворот"),
    DISSOLVE("dissolve", "Растворение"),
    SPIRAL("spiral", "Спираль");

    private final String id;
    private final String displayName;

    TransitionType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TransitionType fromId(String id) {
        for (TransitionType type : values()) {
            if (type.getId().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return FADE;
    }

    public boolean isHeavy() {
        return this == FIREWORK || this == MATRIX || this == RAINBOW || this == GLASS;
    }

    public boolean hasDirection() {
        return this == SLIDE_LEFT || this == SLIDE_RIGHT ||
                this == SLIDE_UP || this == SLIDE_DOWN;
    }

    public TransitionType getOpposite() {
        switch (this) {
            case SLIDE_LEFT: return SLIDE_RIGHT;
            case SLIDE_RIGHT: return SLIDE_LEFT;
            case SLIDE_UP: return SLIDE_DOWN;
            case SLIDE_DOWN: return SLIDE_UP;
            default: return this;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
