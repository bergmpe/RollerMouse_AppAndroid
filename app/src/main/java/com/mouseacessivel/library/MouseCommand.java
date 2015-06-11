package com.mouseacessivel.library;

public enum MouseCommand {
    CLICK(1, "Clique"),
    DOUBLE_CLICK(2, "Duplo clique"),
    HALF_CLICK(3, "Meio Clique"),
    RIGHT_BUTTON_CLICK(4, "Clique bot\u00E3o direito"),

    SCROLL_UP (5, "Rolar para cima"),
    SCROLL_UP_START(6, "Habilitar rolagem para cima"),
    SCROLL_UP_END(7, "Desabilitar rolagem para cima"),

    SCROLL_DOWN (8, "Rolar para baixo"),
    SCROLL_DOWN_START(9, "Habilitar rolagem para baixo"),
    SCROLL_DOWN_END(10, "Desabilitar rolagem para baixo"),

    SCROLL_RIGHT(11, "Rolar para direita"),
    SCROLL_RIGHT_START(12, "Habilitar rolagem para direita"),
    SCROLL_RIGHT_END(13, "Desabilitar rolagem para direita"),

    SCROLL_LEFT(14, "Rolar para esquerda"),
    SCROLL_LEFT_START(15, "Habilitar rolagem para esquerda"),
    SCROLL_LEFT_END(16, "Desabilitar rolagem para esquerda");

    private final int id;
    private final String name;

    /**
     * MouseCommand
     *     constructor
     * @author nta-ifce
     */
    private MouseCommand(int id, String name){
        this.id = id;
        this.name = name;
    }

    /**
     * getValue
     *     returns ID value
     * @author nta-ifce
     */
    public int getID(){
        return this.id;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
