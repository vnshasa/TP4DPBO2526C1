import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Year;
import java.util.ArrayList;

/**
 * PeopleMenu: window utama aplikasi PeopleData (CRUD data anggota komunitas koding).
 *
 * Tampilan form dirancang di PeopleMenu.form (Swing UI Designer),
 * sedangkan semua logic ditulis di file ini.
 * Nama variabel komponen di bawah harus sama dengan properti "field name" di PeopleMenu.form.
 */
public class PeopleMenu extends JFrame {
    // nilai awal slider keahlian
    private static final int DEFAULT_KEAHLIAN = 5;

    // index baris tabel yang sedang dipilih (-1 = tidak ada baris yang dipilih)
    private int selectedIndex = -1;
    // list untuk menampung semua data person
    private ArrayList<Person> listPerson;

    // ===== komponen GUI (terhubung dengan PeopleMenu.form lewat "field name") =====
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JTextField idField;
    private JTextField namaField;
    private JTextField tahunLahirField;
    private JComboBox<String> kategoriComboBox;
    private JSlider keahlianSlider;      // komponen untuk atribut baru (bukan JTextField)
    private JLabel keahlianValueLabel;   // menampilkan angka yang sedang dipilih di slider
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JButton deleteButton;
    private JTable peopleTable;

    // main method
    public static void main(String[] args) {
        // buat object window
        PeopleMenu menu = new PeopleMenu();

        // atur ukuran window
        menu.setSize(700, 600);

        // letakkan window di tengah layar
        menu.setLocationRelativeTo(null);

        // isi window
        menu.setContentPane(menu.mainPanel);

        // ubah warna background
        menu.getContentPane().setBackground(Color.WHITE);

        // tampilkan window
        menu.setVisible(true);

        // agar program ikut berhenti saat window diclose
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // constructor
    public PeopleMenu() {
        // judul window
        setTitle("PeopleData - Data Anggota Komunitas Koding");

        // terjemahkan tombol dialog konfirmasi ke bahasa Indonesia
        UIManager.put("OptionPane.yesButtonText", "Ya");
        UIManager.put("OptionPane.noButtonText", "Tidak");

        // inisialisasi listPerson
        listPerson = new ArrayList<>();

        // isi listPerson
        populateList();

        // isi tabel person
        peopleTable.setModel(setTable());

        // ubah styling title
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        // atur isi combo box
        String[] kategoriData = { "???", "Backend", "Frontend", "Mobile", "Data Science", "DevOps", "UI/UX" };
        kategoriComboBox.setModel(new DefaultComboBoxModel<>(kategoriData));

        // atur slider keahlian (rentang 1 - 10)
        keahlianSlider.setMinimum(1);
        keahlianSlider.setMaximum(10);
        keahlianSlider.setValue(DEFAULT_KEAHLIAN);
        keahlianSlider.setMajorTickSpacing(1);
        keahlianSlider.setPaintTicks(true);
        keahlianSlider.setPaintLabels(true);
        keahlianSlider.setOpaque(false);
        keahlianValueLabel.setText(String.valueOf(keahlianSlider.getValue()));

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // saat slider digeser, angka di sebelahnya ikut berubah
        keahlianSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                keahlianValueLabel.setText(String.valueOf(keahlianSlider.getValue()));
            }
        });

        // saat tombol add/update ditekan
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex == -1) {
                    insertData();
                } else {
                    updateData();
                }
            }
        });

        // saat tombol delete ditekan
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // tidak ada baris yang dipilih, tidak ada yang perlu dihapus
                if (selectedIndex == -1) {
                    return;
                }

                // tampilkan konfirmasi sebelum menghapus data
                int pilihan = JOptionPane.showConfirmDialog(
                        PeopleMenu.this,
                        "Yakin ingin menghapus data \"" + listPerson.get(selectedIndex).getNama() + "\"?",
                        "Konfirmasi Hapus",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                // data hanya dihapus jika user memilih "Ya"
                if (pilihan == JOptionPane.YES_OPTION) {
                    deleteData();
                }
            }
        });

        // saat tombol cancel ditekan
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });

        // saat salah satu baris tabel ditekan
        peopleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ubah selectedIndex menjadi baris tabel yang diklik
                selectedIndex = peopleTable.getSelectedRow();
                if (selectedIndex == -1) {
                    return;
                }

                // ambil data person yang dipilih dari list
                Person p = listPerson.get(selectedIndex);

                // ubah isi textfield, combo box, dan slider
                idField.setText(p.getId());
                namaField.setText(p.getNama());
                tahunLahirField.setText(String.valueOf(p.getTahunLahir()));
                kategoriComboBox.setSelectedItem(p.getKategori());
                keahlianSlider.setValue(p.getKeahlian());

                // ubah button "Add" menjadi "Update"
                addUpdateButton.setText("Update");

                // tampilkan button delete
                deleteButton.setVisible(true);
            }
        });
    }

    // method untuk membuat model tabel dari listPerson
    public final DefaultTableModel setTable() {
        // tentukan kolom tabel
        Object[] cols = { "No", "ID", "Nama", "Tahun Lahir", "Kategori", "Keahlian" };

        // buat objek tabel dengan kolom yang sudah dibuat
        DefaultTableModel tmp = new DefaultTableModel(null, cols) {
            // sel tabel tidak boleh diedit langsung (ubah data lewat form)
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // isi tabel dengan listPerson
        for (int i = 0; i < listPerson.size(); i++) {
            Object[] row = {
                    i + 1,
                    listPerson.get(i).getId(),
                    listPerson.get(i).getNama(),
                    listPerson.get(i).getTahunLahir(),
                    listPerson.get(i).getKategori(),
                    listPerson.get(i).getKeahlian()
            };
            tmp.addRow(row);
        }

        return tmp;
    }

    // method untuk menambah data baru (Create)
    public void insertData() {
        try {
            // ambil value dari textfield, combobox, dan slider
            String id = idField.getText().trim();
            String nama = namaField.getText().trim();
            int tahunLahir = Integer.parseInt(tahunLahirField.getText().trim());
            String kategori = kategoriComboBox.getSelectedItem().toString();
            int keahlian = keahlianSlider.getValue();

            // cek input, jika tidak valid proses dihentikan
            if (!isInputValid(id, nama, tahunLahir, -1)) {
                return;
            }

            // tambahkan data ke dalam list
            listPerson.add(new Person(id, nama, tahunLahir, kategori, keahlian));

            // update tabel
            peopleTable.setModel(setTable());

            // bersihkan form
            clearForm();

            // feedback
            System.out.println("Insert berhasil");
            JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan");
        } catch (NumberFormatException ex) {
            showError("Tahun lahir harus berupa angka!");
        }
    }

    // method untuk mengubah data yang dipilih (Update)
    public void updateData() {
        try {
            // ambil data dari form
            String id = idField.getText().trim();
            String nama = namaField.getText().trim();
            int tahunLahir = Integer.parseInt(tahunLahirField.getText().trim());
            String kategori = kategoriComboBox.getSelectedItem().toString();
            int keahlian = keahlianSlider.getValue();

            // cek input (ID milik data ini sendiri tidak dianggap duplikat)
            if (!isInputValid(id, nama, tahunLahir, selectedIndex)) {
                return;
            }

            // ubah data person di list
            listPerson.get(selectedIndex).setId(id);
            listPerson.get(selectedIndex).setNama(nama);
            listPerson.get(selectedIndex).setTahunLahir(tahunLahir);
            listPerson.get(selectedIndex).setKategori(kategori);
            listPerson.get(selectedIndex).setKeahlian(keahlian);

            // update tabel
            peopleTable.setModel(setTable());

            // bersihkan form
            clearForm();

            // feedback
            System.out.println("Update berhasil");
            JOptionPane.showMessageDialog(this, "Data berhasil diubah");
        } catch (NumberFormatException ex) {
            showError("Tahun lahir harus berupa angka!");
        }
    }

    // method untuk menghapus data yang dipilih (Delete)
    // konfirmasi "Yakin?" ditampilkan di listener deleteButton sebelum method ini dipanggil
    public void deleteData() {
        // hapus data dari list
        listPerson.remove(selectedIndex);

        // update tabel
        peopleTable.setModel(setTable());

        // bersihkan form
        clearForm();

        // feedback
        System.out.println("Delete berhasil");
        JOptionPane.showMessageDialog(this, "Data berhasil dihapus");
    }

    // method untuk mengembalikan form ke keadaan awal
    public void clearForm() {
        // kosongkan semua textfield, combo box, dan slider
        idField.setText("");
        namaField.setText("");
        tahunLahirField.setText("");
        kategoriComboBox.setSelectedIndex(0);
        keahlianSlider.setValue(DEFAULT_KEAHLIAN);

        // hilangkan blok warna pada baris tabel yang dipilih
        peopleTable.clearSelection();

        // ubah button "Update" menjadi "Add"
        addUpdateButton.setText("Add");

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // ubah selectedIndex menjadi -1 (tidak ada baris yang dipilih)
        selectedIndex = -1;
    }

    // method untuk memvalidasi isi form, return true jika semua input valid
    // ignoreIndex = index data yang sedang diubah (-1 jika menambah data baru),
    // supaya ID milik data itu sendiri tidak dianggap duplikat
    private boolean isInputValid(String id, String nama, int tahunLahir, int ignoreIndex) {
        // ID dan nama wajib diisi
        if (id.isEmpty() || nama.isEmpty()) {
            showError("ID dan Nama tidak boleh kosong!");
            return false;
        }

        // tahun lahir harus masuk akal
        int tahunSekarang = Year.now().getValue();
        if (tahunLahir < 1900 || tahunLahir > tahunSekarang) {
            showError("Tahun lahir harus antara 1900 dan " + tahunSekarang + "!");
            return false;
        }

        // kategori harus dipilih (index 0 = "???")
        if (kategoriComboBox.getSelectedIndex() == 0) {
            showError("Silakan pilih kategori terlebih dahulu!");
            return false;
        }

        // ID tidak boleh sama dengan ID person lain
        for (int i = 0; i < listPerson.size(); i++) {
            if (i != ignoreIndex && listPerson.get(i).getId().equalsIgnoreCase(id)) {
                showError("ID \"" + id + "\" sudah digunakan!");
                return false;
            }
        }

        return true;
    }

    // method kecil untuk menampilkan pesan error
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // method untuk mengisi data awal (hardcoded)
    private void populateList() {
        listPerson.add(new Person("KMN001", "Aditya Pratama", 2002, "Backend", 8));
        listPerson.add(new Person("KMN002", "Bunga Maharani", 2003, "Frontend", 7));
        listPerson.add(new Person("KMN003", "Citra Lestari", 2001, "UI/UX", 9));
        listPerson.add(new Person("KMN004", "Dimas Saputra", 2004, "Mobile", 6));
        listPerson.add(new Person("KMN005", "Eka Putri", 2000, "Data Science", 8));
        listPerson.add(new Person("KMN006", "Fajar Nugroho", 1999, "DevOps", 9));
        listPerson.add(new Person("KMN007", "Gita Ramadhani", 2005, "Frontend", 4));
        listPerson.add(new Person("KMN008", "Hendra Wijaya", 2003, "Backend", 5));
        listPerson.add(new Person("KMN009", "Intan Permata", 2004, "Data Science", 6));
        listPerson.add(new Person("KMN010", "Joko Prasetyo", 1998, "Mobile", 10));
    }
}
