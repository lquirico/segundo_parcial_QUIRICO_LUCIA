package modelo;

import java.util.ArrayList;
import java.util.List;

public class Carrito {

    
    private List<CarritoItem> items = new ArrayList<>();

    // agrego un producto con una cantidad
    public void agregarProducto(Producto producto, int cantidad) {
        // si el producto ya esta en el carrito sumo la cantidad
        for (CarritoItem item : items) {
            if (item.getProducto().getNombre().equals(producto.getNombre())) {
                item.setCantidad(item.getCantidad() + cantidad);
                return;
            }
        }
        // si no esta lo agrego como item nuevo
        items.add(new CarritoItem(producto, cantidad));
    }

    // obtengo lista de items
    public List<CarritoItem> getItems() {
        return items;
    }

    // calculo total final
    public double getTotal() {
        return items.stream()
                .mapToDouble(CarritoItem::getSubtotal)
                .sum();
    }
}
