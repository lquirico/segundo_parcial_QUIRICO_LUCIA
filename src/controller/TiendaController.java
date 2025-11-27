package controller;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import modelo.Producto;
import modelo.Carrito;
import modelo.CarritoItem;

public class TiendaController implements Initializable {

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    @FXML private TextField txtCantidad;
    @FXML private ListView<String> listCarrito;

    private List<Producto> listaProductos = new ArrayList<>();

    
    private Carrito carrito = new Carrito();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("stock"));

        cargarProductos();
    }

    private void cargarProductos() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("src/modelo/productos.dat"))) {

            listaProductos = (List<Producto>) ois.readObject();
            tablaProductos.getItems().setAll(listaProductos);

        } catch (Exception e) {
            mostrarError("Error al leer los productos.dat\n" + e.getMessage());
        }
    }

    @FXML
    private void agregarAlCarrito() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarError("Debe seleccionar al menos un producto");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText());
        } catch (NumberFormatException e) {
            mostrarError("La cantidad debe ser un numero");
            return;
        }

        if (cantidad <= 0) {
            mostrarError("La cantidad debe ser mayor a cero");
            return;
        }

        if (cantidad > seleccionado.getStock()) {
            mostrarError("No tenemos stock suficiente");
            return;
        }

        carrito.agregarProducto(seleccionado, cantidad);
        listCarrito.getItems().add(seleccionado.getNombre() + " x " + cantidad);

        txtCantidad.clear();
    }

    @FXML
    private void confirmarCompra() {
        if (carrito.getItems().isEmpty()) {
            mostrarError("El carrito esta vacio");
            return;
        }

        double total = carrito.getTotal();
        StringBuilder ticket = new StringBuilder();

        for (CarritoItem item : carrito.getItems()) {

            double subtotal = item.getSubtotal();

            ticket.append(item.getProducto().getNombre())
                  .append(" - Cantidad: ").append(item.getCantidad())
                  .append(" - Subtotal: $").append(subtotal)
                  .append("\n");

            // actualizar stock
            item.getProducto().setStock(
                item.getProducto().getStock() - item.getCantidad()
            );
        }

        ticket.append("\nTOTAL A PAGAR: $").append(total);

        try (PrintWriter pw = new PrintWriter("ticket.txt")) {
            pw.println(ticket.toString());
        } catch (Exception e) {
            mostrarError("No se pudo guardar el ticket");
            return;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("src/modelo/productos.dat"))) {

            oos.writeObject(listaProductos);

        } catch (Exception e) {
            mostrarError("Error al actualizar productos.dat");
            return;
        }
        
        //limpio el carrito

        carrito.getItems().clear();
        listCarrito.getItems().clear();
        tablaProductos.refresh();

        Alert ok = new Alert(Alert.AlertType.INFORMATION);
        ok.setTitle("Compra realizada");
        ok.setHeaderText(null);
        ok.setContentText("El ticket fue generado y el stock se actualizo correctamente");
        ok.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
