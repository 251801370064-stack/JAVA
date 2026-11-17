package com.library.view;

import com.library.model.Book;
import com.library.service.BookService;
import com.library.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class BooksPanel extends JPanel implements Refreshable {
    private final BookService service = new BookService();
    private final JTable table = UiUtil.table("ID", "ISBN", "Title", "Author", "Publisher", "Category", "Price", "Quantity", "Available");
    private final JTextField search = new JTextField(22);
    private final JComboBox<String> sort = new JComboBox<>(new String[]{"Title", "Author", "Most available", "Newest"});
    private List<Book> records = new ArrayList<>();

    public BooksPanel() {
        setLayout(new BorderLayout(0, 16)); setBackground(UiUtil.PALE); setBorder(BorderFactory.createEmptyBorder(24, 28, 26, 28));
        JPanel heading = new JPanel(new BorderLayout(0, 5)); heading.setOpaque(false); heading.add(UiUtil.title("Books"), BorderLayout.NORTH); heading.add(UiUtil.muted("Catalog, inventory and availability"), BorderLayout.CENTER); add(heading, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 12)); body.setOpaque(false);
        JPanel toolbar = new JPanel(new BorderLayout(10, 0)); toolbar.setOpaque(false);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); filters.setOpaque(false);
        search.setToolTipText("Search title, author, ISBN or book ID"); search.setPreferredSize(new java.awt.Dimension(250, 36));
        filters.add(new JLabel("Search")); filters.add(search); filters.add(new JLabel("Sort")); filters.add(sort); toolbar.add(filters, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actions.setOpaque(false);
        var add = UiUtil.button("Add book", true); var edit = UiUtil.button("Edit", false); var delete = UiUtil.button("Delete", false);
        actions.add(add); actions.add(edit); actions.add(delete); toolbar.add(actions, BorderLayout.EAST); body.add(toolbar, BorderLayout.NORTH); body.add(UiUtil.scroll(table), BorderLayout.CENTER); add(body, BorderLayout.CENTER);
        add.addActionListener(event -> editBook(null)); edit.addActionListener(event -> editBook(selected())); delete.addActionListener(event -> deleteBook());
        sort.addActionListener(event -> refreshData());
        search.getDocument().addDocumentListener(new DocumentListener() { public void insertUpdate(DocumentEvent e) { refreshData(); } public void removeUpdate(DocumentEvent e) { refreshData(); } public void changedUpdate(DocumentEvent e) { refreshData(); } });
        refreshData();
    }

    @Override public void refreshData() {
        try {
            String order = switch (sort.getSelectedIndex()) { case 1 -> "author"; case 2 -> "available"; case 3 -> "newest"; default -> "title"; };
            records = service.search(search.getText(), order); DefaultTableModel model = (DefaultTableModel) table.getModel(); model.setRowCount(0);
            for (Book book : records) model.addRow(new Object[]{book.id(), book.isbn(), book.title(), book.author(), book.publisher(), book.category(), book.price(), book.quantity(), book.availableQuantity()});
        } catch (Exception exception) { UiUtil.error(this, exception); }
    }

    private Book selected() {
        int row = table.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(this, "Select a book first.", "No selection", JOptionPane.WARNING_MESSAGE); return null; }
        return records.get(table.convertRowIndexToModel(row));
    }

    private void editBook(Book original) {
        JTextField isbn = new JTextField(original == null ? "" : original.isbn()); JTextField title = new JTextField(original == null ? "" : original.title());
        JTextField author = new JTextField(original == null ? "" : original.author()); JTextField publisher = new JTextField(original == null ? "" : original.publisher());
        JTextField category = new JTextField(original == null ? "" : original.category()); JTextField price = new JTextField(original == null ? "0.00" : original.price().toPlainString());
        JTextField quantity = new JTextField(original == null ? "1" : Integer.toString(original.quantity()));
        JPanel form = new JPanel(new GridLayout(0, 2, 9, 8)); form.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 5));
        addField(form, "ISBN", isbn); addField(form, "Title", title); addField(form, "Author", author); addField(form, "Publisher", publisher); addField(form, "Category", category); addField(form, "Price", price); addField(form, "Total copies", quantity);
        int choice = JOptionPane.showConfirmDialog(this, form, original == null ? "Add book" : "Edit book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;
        try {
            Book input = new Book(original == null ? 0 : original.id(), ValidationUtil.isbn(isbn.getText()), title.getText().trim(), author.getText().trim(), publisher.getText().trim(), category.getText().trim(), ValidationUtil.nonNegativeMoney(price.getText(), "Price"), ValidationUtil.nonNegativeInt(quantity.getText(), "Quantity"), 0);
            service.save(input); refreshData(); UiUtil.success(this, original == null ? "Book added." : "Book updated.");
        } catch (Exception exception) { UiUtil.error(this, exception); }
    }

    private void deleteBook() {
        Book book = selected(); if (book == null) return;
        if (JOptionPane.showConfirmDialog(this, "Delete '" + book.title() + "'?", "Confirm deletion", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { service.delete(book.id()); refreshData(); } catch (Exception exception) { UiUtil.error(this, exception); }
    }

    private void addField(JPanel panel, String label, JTextField input) { panel.add(new JLabel(label)); input.setColumns(20); panel.add(input); }
}
