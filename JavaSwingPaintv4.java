import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.function.Consumer;

public class JavaSwingPaintv4 extends JFrame {
	private PanouDesenare panouDesenare;
        private JButton butonCuloare, butonCuloareGradient;
        private JButton butonUndo, butonRedo;

	private JComboBox<String> comboUmplere;

	private Color culoareCurenta = Color.BLACK;
	private Color culoareGradient = Color.WHITE;
	private String primitivaCurenta = "LINIE";

	private JTextField tfDash;
	private JTextField tfPhase;
	private JButton btnAplicaStil;

	private JSpinner spinnerGrosimeLinie;
	private ButtonGroup grupPrimitive;

	public JavaSwingPaintv4() {
		setTitle("Aplicație Desen Graphics2D - Ex3.3");
		setSize(1400, 800);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		panouDesenare = new PanouDesenare();

		JPanel panelNord = new JPanel(new BorderLayout());
		panelNord.add(createToolBar(), BorderLayout.NORTH);
		panelNord.add(createSecondToolBar(), BorderLayout.SOUTH);

		add(panelNord, BorderLayout.NORTH);
		add(panouDesenare, BorderLayout.CENTER);
		add(createOperationsPanel(), BorderLayout.EAST);

		setVisible(true);
	}

	private JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);

		grupPrimitive = new ButtonGroup();

		adaugaToggleButton(toolBar, "Linie", "LINIE");
		adaugaToggleButton(toolBar, "Dreptunghi", "DREPTUNGHI");
		adaugaToggleButton(toolBar, "Dreptunghi Rotund", "DREPTUNGHI_ROTUND");

		toolBar.addSeparator();

		adaugaToggleButton(toolBar, "Elipsă", "ELIPSA");
		adaugaToggleButton(toolBar, "Arc Elipsă", "ARC_ELIPSA");
		adaugaToggleButton(toolBar, "Segment Elipsă", "SEGMENT_ELIPSA");
		adaugaToggleButton(toolBar, "Sector Elipsă", "SECTOR_ELIPSA");

		toolBar.addSeparator();

		adaugaToggleButton(toolBar, "Curbă Pătratică", "CURBA_PATRATA");
		adaugaToggleButton(toolBar, "Curbă Cubică", "CURBA_CUBICA");
		adaugaToggleButton(toolBar, "Poligon", "POLIGON");

		toolBar.addSeparator();

		adaugaToggleButton(toolBar, "Imagine", "IMAGINE");
		adaugaToggleButton(toolBar, "Text", "TEXT");
		adaugaToggleButton(toolBar, "Text ca Formă", "TEXT_FORMA");

		return toolBar;
	}

        private JToolBar createSecondToolBar() {
                JToolBar toolBar = new JToolBar();
                toolBar.setFloatable(false);

                toolBar.add(new JLabel("Culoare:"));
		butonCuloare = new JButton("  ");
		butonCuloare.setBackground(culoareCurenta);
		butonCuloare.setPreferredSize(new Dimension(40, 25));
		butonCuloare.addActionListener(e -> selecteazaCuloare());
		toolBar.add(butonCuloare);

		toolBar.addSeparator();

		toolBar.add(new JLabel("Culoare 2 (Gradient):"));
		butonCuloareGradient = new JButton("  ");
		butonCuloareGradient.setBackground(culoareGradient);
		butonCuloareGradient.setPreferredSize(new Dimension(40, 25));
		butonCuloareGradient.addActionListener(e -> selecteazaCuloareGradient());
		toolBar.add(butonCuloareGradient);

		toolBar.addSeparator();

		toolBar.addSeparator();

		toolBar.add(new JLabel("Pattern (ex: 10,3,3,3):"));
		tfDash = new JTextField("0");
		tfDash.setPreferredSize(new Dimension(120, 25));
		toolBar.add(tfDash);

		toolBar.addSeparator();

		toolBar.add(new JLabel("Defazaj:"));
		tfPhase = new JTextField("0");
		tfPhase.setPreferredSize(new Dimension(60, 25));
		toolBar.add(tfPhase);

		toolBar.addSeparator();

		btnAplicaStil = new JButton("Aplică stil");
		btnAplicaStil.addActionListener(e -> {
			float[] dash = parseDash(tfDash.getText());
			float phase = parseFloatSafe(tfPhase.getText(), 0f);
			panouDesenare.setDash(dash, phase);
			panouDesenare.actualizeazaLinieStilSelectie(dash, phase);
			panouDesenare.repaint();
		});
		toolBar.add(btnAplicaStil);

		toolBar.add(new JLabel("Grosime:"));
		spinnerGrosimeLinie = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
		spinnerGrosimeLinie.setPreferredSize(new Dimension(60, 25));
		spinnerGrosimeLinie.addChangeListener(e -> {
			int grosime = (Integer) spinnerGrosimeLinie.getValue();
			panouDesenare.setGrosimeLinie(grosime);
			panouDesenare.actualizeazaGrosimeLinieSelectie(grosime);
		});
		toolBar.add(spinnerGrosimeLinie);

		toolBar.addSeparator();

		toolBar.add(new JLabel("Umplere:"));
		comboUmplere = new JComboBox<>(new String[] { "Fără Umplere", "Culoare Solidă", "Gradient Aciclic",
				"Gradient Ciclic", "Textură Desen", "Textură Imagine" });
		comboUmplere.addActionListener(e -> {
			int tip = comboUmplere.getSelectedIndex();
			panouDesenare.setTipUmplere(tip);
			panouDesenare.actualizeazaTipUmplereSelectie(tip);
			if (tip == 5) {
				selecteazaTexturaImagine();
			}
		});
		toolBar.add(comboUmplere);

		toolBar.addSeparator();

                JButton butonSterge = new JButton("Șterge Tot");
                butonSterge.addActionListener(e -> panouDesenare.stergeTot());
                toolBar.add(butonSterge);

                toolBar.addSeparator();

                butonUndo = new JButton("Undo");
                butonUndo.addActionListener(e -> panouDesenare.undo());
                toolBar.add(butonUndo);

                butonRedo = new JButton("Redo");
                butonRedo.addActionListener(e -> panouDesenare.redo());
                toolBar.add(butonRedo);

                toolBar.addSeparator();

                JButton butonSalveaza = new JButton("Salvează");
                butonSalveaza.addActionListener(e -> salveazaDesen());
                toolBar.add(butonSalveaza);

                JButton butonIncarca = new JButton("Încarcă");
                butonIncarca.addActionListener(e -> incarcaDesen());
                toolBar.add(butonIncarca);

                actualizeazaButoaneUndoRedo();

                return toolBar;
        }

        private void actualizeazaButoaneUndoRedo() {
                if (butonUndo != null) {
                        butonUndo.setEnabled(panouDesenare != null && panouDesenare.canUndo());
                }
                if (butonRedo != null) {
                        butonRedo.setEnabled(panouDesenare != null && panouDesenare.canRedo());
                }
        }

        private void salveazaDesen() {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                fileChooser.setFileFilter(new FileNameExtensionFilter("Desene (*.ser)", "ser"));

                int result = fileChooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                        File fisier = fileChooser.getSelectedFile();
                        if (!fisier.getName().toLowerCase().endsWith(".ser")) {
                                fisier = new File(fisier.getParentFile(), fisier.getName() + ".ser");
                        }
                        try {
                                panouDesenare.salveazaInFisier(fisier);
                                JOptionPane.showMessageDialog(this, "Desen salvat cu succes.");
                        } catch (IOException ex) {
                                JOptionPane.showMessageDialog(this,
                                                "Nu s-a putut salva desenul: " + ex.getMessage(),
                                                "Eroare", JOptionPane.ERROR_MESSAGE);
                        }
                }
        }

        private void incarcaDesen() {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                fileChooser.setFileFilter(new FileNameExtensionFilter("Desene (*.ser)", "ser"));

                int result = fileChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                        File fisier = fileChooser.getSelectedFile();
                        try {
                                panouDesenare.incarcaDinFisier(fisier);
                                actualizeazaButoaneUndoRedo();
                                repaint();
                                JOptionPane.showMessageDialog(this, "Desen încărcat cu succes.");
                        } catch (IOException | ClassNotFoundException ex) {
                                JOptionPane.showMessageDialog(this,
                                                "Nu s-a putut încărca desenul: " + ex.getMessage(),
                                                "Eroare", JOptionPane.ERROR_MESSAGE);
                        }
                }
        }

	private JPanel createOperationsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Operații pe Forma Selectată"));
		panel.setPreferredSize(new Dimension(200, 0));

		panel.add(Box.createVerticalStrut(10));

		JButton btnTranslatie = new JButton("Translație");
		btnTranslatie.setMaximumSize(new Dimension(180, 30));
		btnTranslatie.addActionListener(e -> aplicaTranslatie());
		panel.add(btnTranslatie);

		panel.add(Box.createVerticalStrut(5));

		JButton btnRotatie = new JButton("Rotație");
		btnRotatie.setMaximumSize(new Dimension(180, 30));
		btnRotatie.addActionListener(e -> aplicaRotatie());
		panel.add(btnRotatie);

		panel.add(Box.createVerticalStrut(5));

		JButton btnScalare = new JButton("Scalare");
		btnScalare.setMaximumSize(new Dimension(180, 30));
		btnScalare.addActionListener(e -> aplicaScalare());
		panel.add(btnScalare);

		panel.add(Box.createVerticalStrut(5));

		JButton btnInclinare = new JButton("Înclinare");
		btnInclinare.setMaximumSize(new Dimension(180, 30));
		btnInclinare.addActionListener(e -> aplicaInclinare());
		panel.add(btnInclinare);

		panel.add(Box.createVerticalStrut(15));
		panel.add(new JSeparator());
		panel.add(Box.createVerticalStrut(10));

		JLabel lblArii = new JLabel("Operații cu Arii:");
		panel.add(lblArii);
		panel.add(Box.createVerticalStrut(5));

		JButton btnReuniune = new JButton("Reuniune");
		btnReuniune.setMaximumSize(new Dimension(180, 30));
		btnReuniune.addActionListener(e -> aplicaOperatieArie("REUNIUNE"));
		panel.add(btnReuniune);

		panel.add(Box.createVerticalStrut(5));

		JButton btnExtragere = new JButton("Extragere");
		btnExtragere.setMaximumSize(new Dimension(180, 30));
		btnExtragere.addActionListener(e -> aplicaOperatieArie("EXTRAGERE"));
		panel.add(btnExtragere);

		panel.add(Box.createVerticalStrut(5));

		JButton btnIntersectie = new JButton("Intersecție");
		btnIntersectie.setMaximumSize(new Dimension(180, 30));
		btnIntersectie.addActionListener(e -> aplicaOperatieArie("INTERSECTIE"));
		panel.add(btnIntersectie);

		panel.add(Box.createVerticalStrut(5));

		JButton btnXor = new JButton("Sau Exclusiv (XOR)");
		btnXor.setMaximumSize(new Dimension(180, 30));
		btnXor.addActionListener(e -> aplicaOperatieArie("XOR"));
		panel.add(btnXor);

		panel.add(Box.createVerticalStrut(15));
		panel.add(new JSeparator());
		panel.add(Box.createVerticalStrut(10));

		JButton btnDecupare = new JButton("Decupare Zonă");
		btnDecupare.setMaximumSize(new Dimension(180, 30));
		btnDecupare.addActionListener(e -> activeazaDecupare());
		panel.add(btnDecupare);

		panel.add(Box.createVerticalGlue());

		return panel;
	}

	private static float[] parseDash(String s) {
		if (s == null)
			return null;
		s = s.trim();
		if (s.isEmpty())
			return null; // fără model => linie plină
		String[] parts = s.split(",");
		float[] dash = new float[parts.length];
		for (int i = 0; i < parts.length; i++) {
			dash[i] = Float.parseFloat(parts[i].trim());
		}
		// validare: toate valorile > 0
		for (float v : dash)
			if (v <= 0f)
				return null;
		return dash;
	}

	private static float parseFloatSafe(String s, float def) {
		try {
			return Float.parseFloat(s.trim());
		} catch (Exception ex) {
			return def;
		}
	}

	private void adaugaToggleButton(JToolBar toolBar, String text, final String primitiva) {
		JToggleButton buton = new JToggleButton(text);
		grupPrimitive.add(buton);
		buton.addActionListener(e -> {
			primitivaCurenta = primitiva;
			panouDesenare.setPrimitiva(primitiva);
		});
		toolBar.add(buton);
		if (primitiva.equals("LINIE")) {
			buton.setSelected(true);
		}
	}

        private void selecteazaCuloare() {
                Color culoareNoua = JColorChooser.showDialog(this, "Selectează Culoare", culoareCurenta);
                if (culoareNoua != null) {
                        culoareCurenta = culoareNoua;
                        butonCuloare.setBackground(culoareCurenta);
                        panouDesenare.setCuloare(culoareCurenta);
                        panouDesenare.actualizeazaCuloareFormaSelectata(culoareCurenta);
                }
        }

        private void selecteazaCuloareGradient() {
                Color culoareNoua = JColorChooser.showDialog(this, "Selectează Culoare pentru Gradient", culoareGradient);
                if (culoareNoua != null) {
                        culoareGradient = culoareNoua;
                        butonCuloareGradient.setBackground(culoareGradient);
                        panouDesenare.setCuloareGradient(culoareGradient);
                        panouDesenare.actualizeazaCuloareGradientFormaSelectata(culoareGradient);
                }
        }

	private void selecteazaTexturaImagine() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Imagini (*.jpg, *.png, *.gif)", "jpg", "jpeg",
				"png", "gif");
		fileChooser.setFileFilter(filter);

		int result = fileChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                        File fisier = fileChooser.getSelectedFile();
                        ImageIcon icon = new ImageIcon(fisier.getAbsolutePath());
                        panouDesenare.setTexturaImagine(icon.getImage());
                        panouDesenare.actualizeazaTexturaSelectie(icon.getImage());
                }
        }

	private void aplicaTranslatie() {
		String input = JOptionPane.showInputDialog(this, "Introduceți translația (dx,dy):", "50,30");
		if (input != null && !input.isEmpty()) {
			String[] parts = input.split(",");
			if (parts.length == 2) {
				try {
					double dx = Double.parseDouble(parts[0].trim());
					double dy = Double.parseDouble(parts[1].trim());
					panouDesenare.aplicaTransformareFormaSelectata(AffineTransform.getTranslateInstance(dx, dy));
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(this, "Format invalid!");
				}
			}
		}
	}

	private void aplicaRotatie() {
		String input = JOptionPane.showInputDialog(this, "Introduceți unghiul de rotație (grade):", "45");
		if (input != null && !input.isEmpty()) {
			try {
				double unghi = Double.parseDouble(input.trim());
				panouDesenare.aplicaRotatieFormaSelectata(Math.toRadians(unghi));
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Format invalid!");
			}
		}
	}

	private void aplicaScalare() {
		String input = JOptionPane.showInputDialog(this, "Introduceți factorul de scalare (sx,sy):", "1.5,1.5");
		if (input != null && !input.isEmpty()) {
			String[] parts = input.split(",");
			if (parts.length == 2) {
				try {
					double sx = Double.parseDouble(parts[0].trim());
					double sy = Double.parseDouble(parts[1].trim());
					panouDesenare.aplicaScalareFormaSelectata(sx, sy);
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(this, "Format invalid!");
				}
			}
		}
	}

	private void aplicaInclinare() {
		String input = JOptionPane.showInputDialog(this, "Introduceți factorii de înclinare (shx,shy):", "0.3,0");
		if (input != null && !input.isEmpty()) {
			String[] parts = input.split(",");
			if (parts.length == 2) {
				try {
					double shx = Double.parseDouble(parts[0].trim());
					double shy = Double.parseDouble(parts[1].trim());
					panouDesenare.aplicaInclinareFormaSelectata(shx, shy);
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(this, "Format invalid!");
				}
			}
		}
	}

	private void aplicaOperatieArie(String operatie) {
		panouDesenare.aplicaOperatieArie(operatie);
	}

	private void activeazaDecupare() {
		panouDesenare.activeazaModDecupare();
	}

	class PanouDesenare extends JPanel {
		private ArrayList<Forma> forme;
		private Point punctStart;
		private Point punctCurent;
		private Point punctControl1, punctControl2;
		private String primitivaCurenta;
		private Color culoareCurenta, culoareGradient;
		private ArrayList<Point> punctePoligon;
		private Font fontCurent;

		private float[] dash = null; // null = linie plină
		private float dashPhase = 0f;

		private int grosimeLinie = 2;
		private int tipUmplere = 0;
		private Image texturaImagine;
                private int formaSelectata = -1;
                private int formaSelectata2 = -1;
                private boolean modDecupare = false;
                private Shape zonaDecupare = null;
                private int etapaCurba = 0;

                private Deque<StarePanou> undoStack;
                private Deque<StarePanou> redoStack;

                public PanouDesenare() {
                        forme = new ArrayList<>();
                        punctePoligon = new ArrayList<>();
                        primitivaCurenta = "LINIE";
                        culoareCurenta = Color.BLACK;
                        culoareGradient = Color.WHITE;
                        fontCurent = new Font("SansSerif", Font.PLAIN, 24);

                        undoStack = new ArrayDeque<>();
                        redoStack = new ArrayDeque<>();

                        setBackground(Color.WHITE);

                        addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					if (modDecupare) {
						punctStart = e.getPoint();
						return;
					}

					if (SwingUtilities.isRightMouseButton(e)) {
						if (primitivaCurenta.equals("POLIGON")) {
							finalizeazaPoligon();
						} else if (primitivaCurenta.equals("CURBA_PATRATA")
								|| primitivaCurenta.equals("CURBA_CUBICA")) {
							etapaCurba = 0;
							punctStart = null;
							punctControl1 = null;
							punctControl2 = null;
							repaint();
						}
					} else if (SwingUtilities.isLeftMouseButton(e)) {
						// Verifică dacă s-a dat click pe o formă existentă (selectăm până la 2 forme)
						int index = -1;
						for (int i = forme.size() - 1; i >= 0; i--) {
							if (forme.get(i).contine(e.getPoint())) {
								index = i;
								break;
							}
						}
						if (index != -1) {
							if (formaSelectata == -1 || (formaSelectata != -1 && formaSelectata2 != -1)) {
								// începem o nouă pereche
								formaSelectata = index;
								formaSelectata2 = -1;
							} else if (index != formaSelectata) {
								// a doua selecție diferită de prima
								formaSelectata2 = index;
							}
							repaint();
							return;
						}

						if (formaSelectata != -1 || formaSelectata2 != -1) {
							formaSelectata = -1;
							formaSelectata2 = -1;
							repaint();
						}

						if (primitivaCurenta.equals("POLIGON")) {
							punctePoligon.add(e.getPoint());
							repaint();
						} else if (primitivaCurenta.equals("CURBA_PATRATA")) {
							if (etapaCurba == 0) {
								punctStart = e.getPoint();
								etapaCurba = 1;
							} else if (etapaCurba == 1) {
								punctControl1 = e.getPoint();
								etapaCurba = 2;
                                                        } else if (etapaCurba == 2) {
                                                                Forma forma = new Forma(primitivaCurenta, culoareCurenta, culoareGradient, punctStart,
                                                                                e.getPoint(), punctControl1, null, fontCurent, dash, dashPhase, grosimeLinie,
                                                                                tipUmplere, texturaImagine);
                                                                salveazaStarePentruUndo();
                                                                forme.add(forma);
                                                                etapaCurba = 0;
                                                                punctStart = null;
                                                                punctControl1 = null;
                                                                repaint();
							}
						} else if (primitivaCurenta.equals("CURBA_CUBICA")) {
							if (etapaCurba == 0) {
								punctStart = e.getPoint();
								etapaCurba = 1;
							} else if (etapaCurba == 1) {
								punctControl1 = e.getPoint();
								etapaCurba = 2;
							} else if (etapaCurba == 2) {
								punctControl2 = e.getPoint();
								etapaCurba = 3;
                                                        } else if (etapaCurba == 3) {
                                                                Forma forma = new Forma(primitivaCurenta, culoareCurenta, culoareGradient, punctStart,
                                                                                e.getPoint(), punctControl1, punctControl2, fontCurent, dash, dashPhase,
                                                                                grosimeLinie, tipUmplere, texturaImagine);
                                                                salveazaStarePentruUndo();
                                                                forme.add(forma);
                                                                etapaCurba = 0;
                                                                punctStart = null;
                                                                punctControl1 = null;
                                                                punctControl2 = null;
								repaint();
							}
						} else if (primitivaCurenta.equals("IMAGINE")) {
							selecteazaImagine(e.getPoint());
						} else if (primitivaCurenta.equals("TEXT")) {
							adaugaText(e.getPoint());
						} else if (primitivaCurenta.equals("TEXT_FORMA")) {
							adaugaTextForma(e.getPoint());
						} else {
							punctStart = e.getPoint();
						}
					}
				}

				public void mouseReleased(MouseEvent e) {
                                        if (modDecupare && punctStart != null && punctCurent != null) {
                                                salveazaStarePentruUndo();
                                                int x = Math.min(punctStart.x, punctCurent.x);
                                                int y = Math.min(punctStart.y, punctCurent.y);
                                                int width = Math.abs(punctCurent.x - punctStart.x);
                                                int height = Math.abs(punctCurent.y - punctStart.y);
                                                zonaDecupare = new Rectangle2D.Double(x, y, width, height);
						modDecupare = false;
						punctStart = null;
						punctCurent = null;
						repaint();
						return;
					}

					if (SwingUtilities.isLeftMouseButton(e) && !primitivaCurenta.equals("POLIGON")
							&& !primitivaCurenta.equals("CURBA_PATRATA") && !primitivaCurenta.equals("CURBA_CUBICA")
							&& !primitivaCurenta.equals("IMAGINE") && !primitivaCurenta.equals("TEXT")
							&& !primitivaCurenta.equals("TEXT_FORMA")) {
                                                if (punctStart != null) {
                                                        Forma forma = new Forma(primitivaCurenta, culoareCurenta, culoareGradient, punctStart,
                                                                        e.getPoint(), null, null, fontCurent, dash, dashPhase, grosimeLinie, tipUmplere,
                                                                        texturaImagine);
                                                        salveazaStarePentruUndo();
                                                        forme.add(forma);
                                                        punctStart = null;
                                                        punctCurent = null;
                                                        repaint();
                                                }
					}
				}
			});

                        addMouseMotionListener(new MouseMotionAdapter() {
                                public void mouseDragged(MouseEvent e) {
                                        if (!primitivaCurenta.equals("POLIGON") && !primitivaCurenta.equals("CURBA_PATRATA")
                                                        && !primitivaCurenta.equals("CURBA_CUBICA") && !primitivaCurenta.equals("IMAGINE")
                                                        && !primitivaCurenta.equals("TEXT") && !primitivaCurenta.equals("TEXT_FORMA")) {
                                                punctCurent = e.getPoint();
                                                repaint();
                                        }
                                }
                        });
                }

                private class StarePanou {
                        private final ArrayList<Forma> forme;
                        private final Shape zonaDecupare;

                        private StarePanou(ArrayList<Forma> forme, Shape zonaDecupare) {
                                this.forme = forme;
                                this.zonaDecupare = zonaDecupare;
                        }
                }

                private void salveazaStarePentruUndo() {
                        undoStack.push(creeazaStareCurenta());
                        redoStack.clear();
                        JavaSwingPaintv4.this.actualizeazaButoaneUndoRedo();
                }

                private StarePanou creeazaStareCurenta() {
                        ArrayList<Forma> copieForme = copiazaForme(forme);
                        Shape copieZona = copieZona(zonaDecupare);
                        return new StarePanou(copieForme, copieZona);
                }

                private ArrayList<Forma> copiazaForme(ArrayList<Forma> sursa) {
                        ArrayList<Forma> copie = new ArrayList<>();
                        for (Forma forma : sursa) {
                                copie.add(new Forma(forma));
                        }
                        return copie;
                }

                private Shape copieZona(Shape zona) {
                        if (zona instanceof Rectangle2D) {
                                Rectangle2D rect = (Rectangle2D) zona;
                                return new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
                        }
                        return null;
                }

                private void restaureazaStare(StarePanou stare) {
                        forme = copiazaForme(stare.forme);
                        zonaDecupare = copieZona(stare.zonaDecupare);
                        punctePoligon.clear();
                        punctStart = null;
                        punctCurent = null;
                        punctControl1 = null;
                        punctControl2 = null;
                        etapaCurba = 0;
                        formaSelectata = -1;
                        formaSelectata2 = -1;
                        repaint();
                }

                public boolean canUndo() {
                        return !undoStack.isEmpty();
                }

                public boolean canRedo() {
                        return !redoStack.isEmpty();
                }

                public boolean undo() {
                        if (undoStack.isEmpty()) {
                                return false;
                        }
                        redoStack.push(creeazaStareCurenta());
                        StarePanou stareAnterioara = undoStack.pop();
                        restaureazaStare(stareAnterioara);
                        JavaSwingPaintv4.this.actualizeazaButoaneUndoRedo();
                        return true;
                }

                public boolean redo() {
                        if (redoStack.isEmpty()) {
                                return false;
                        }
                        undoStack.push(creeazaStareCurenta());
                        StarePanou stareUrmatoare = redoStack.pop();
                        restaureazaStare(stareUrmatoare);
                        JavaSwingPaintv4.this.actualizeazaButoaneUndoRedo();
                        return true;
                }

                public void salveazaInFisier(File fisier) throws IOException {
                        DesenSerializat desen = new DesenSerializat();
                        desen.forme = new ArrayList<>();
                        for (Forma forma : forme) {
                                desen.forme.add(forma.toSerializat());
                        }
                        desen.zonaDecupare = RectangleData.fromShape(zonaDecupare);

                        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fisier))) {
                                oos.writeObject(desen);
                        }
                }

                public void incarcaDinFisier(File fisier) throws IOException, ClassNotFoundException {
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fisier))) {
                                Object obj = ois.readObject();
                                if (!(obj instanceof DesenSerializat)) {
                                        throw new IOException("Fișier invalid");
                                }
                                DesenSerializat desen = (DesenSerializat) obj;
                                ArrayList<Forma> formeNoi = new ArrayList<>();
                                if (desen.forme != null) {
                                        for (FormaSerializata serializata : desen.forme) {
                                                formeNoi.add(new Forma(serializata));
                                        }
                                }
                                salveazaStareCurentaPentruUndoLaIncarcare();
                                forme = formeNoi;
                                zonaDecupare = desen.zonaDecupare != null ? desen.zonaDecupare.toShape() : null;
                                punctePoligon.clear();
                                punctStart = null;
                                punctCurent = null;
                                punctControl1 = null;
                                punctControl2 = null;
                                etapaCurba = 0;
                                formaSelectata = -1;
                                formaSelectata2 = -1;
                                redoStack.clear();
                                repaint();
                                JavaSwingPaintv4.this.actualizeazaButoaneUndoRedo();
                        }
                }

                private void salveazaStareCurentaPentruUndoLaIncarcare() {
                        if (!forme.isEmpty() || zonaDecupare != null) {
                                undoStack.push(creeazaStareCurenta());
                                redoStack.clear();
                        }
                }

                public void setDash(float[] dash, float phase) {
                        this.dash = dash;
                        this.dashPhase = phase;
                }

		private Stroke creeazaStrokeCustom() {
			if (dash != null && dash.length > 0) {
				return new BasicStroke(grosimeLinie, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash,
						dashPhase);
			}
			return new BasicStroke(grosimeLinie);
		}

		public void setPrimitiva(String primitiva) {
			this.primitivaCurenta = primitiva;
			punctePoligon.clear();
			etapaCurba = 0;
			punctStart = null;
			punctControl1 = null;
			punctControl2 = null;
			formaSelectata = -1;
			formaSelectata2 = -1;
			repaint();
		}

		public void setCuloare(Color culoare) {
			this.culoareCurenta = culoare;
		}

		public void setCuloareGradient(Color culoare) {
			this.culoareGradient = culoare;
		}

		public void setGrosimeLinie(int grosime) {
			this.grosimeLinie = grosime;
		}

                public void setTipUmplere(int tip) {
                        this.tipUmplere = tip;
                }

                public void setTexturaImagine(Image img) {
                        this.texturaImagine = img;
                }

                public void actualizeazaCuloareFormaSelectata(Color culoare) {
                        modificaFormeSelectate(forma -> forma.setCuloare(culoare));
                }

                public void actualizeazaCuloareGradientFormaSelectata(Color culoare) {
                        modificaFormeSelectate(forma -> forma.setCuloareGradient(culoare));
                }

                public void actualizeazaGrosimeLinieSelectie(int grosime) {
                        modificaFormeSelectate(forma -> forma.setGrosimeLinie(grosime));
                }

                public void actualizeazaLinieStilSelectie(float[] dash, float phase) {
                        modificaFormeSelectate(forma -> forma.setDashPattern(dash, phase));
                }

                public void actualizeazaTipUmplereSelectie(int tip) {
                        modificaFormeSelectate(forma -> forma.setTipUmplere(tip));
                }

                public void actualizeazaTexturaSelectie(Image textura) {
                        modificaFormeSelectate(forma -> forma.setTexturaImagine(textura));
                }

                private void modificaFormeSelectate(Consumer<Forma> modificare) {
                        Forma prima = null;
                        Forma aDoua = null;

                        if (formaSelectata >= 0 && formaSelectata < forme.size()) {
                                prima = forme.get(formaSelectata);
                        }
                        if (formaSelectata2 >= 0 && formaSelectata2 < forme.size()) {
                                aDoua = forme.get(formaSelectata2);
                        }

                        if (prima == null && aDoua == null) {
                                return;
                        }

                        salveazaStarePentruUndo();
                        if (prima != null) {
                                modificare.accept(prima);
                        }
                        if (aDoua != null && aDoua != prima) {
                                modificare.accept(aDoua);
                        }
                        repaint();
                }

                public void stergeTot() {
                        if (!forme.isEmpty() || zonaDecupare != null) {
                                salveazaStarePentruUndo();
                        }
                        forme.clear();
                        punctePoligon.clear();
                        formaSelectata = -1;
                        formaSelectata2 = -1;
                        zonaDecupare = null;
                        repaint();
                }

		public void activeazaModDecupare() {
			modDecupare = true;
			JOptionPane.showMessageDialog(JavaSwingPaintv4.this,
					"Mod decupare activat. Desenați un dreptunghi pentru zona de decupare.");
		}

                public void aplicaTransformareFormaSelectata(AffineTransform transform) {
                        if (formaSelectata >= 0 && formaSelectata < forme.size()) {
                                salveazaStarePentruUndo();
                                forme.get(formaSelectata).aplicaTransformare(transform);
                                repaint();
                        } else {
                                JOptionPane.showMessageDialog(JavaSwingPaintv4.this, "Selectați mai întâi o formă dând click pe ea!");
                        }
		}

                public void aplicaRotatieFormaSelectata(double unghi) {
                        if (formaSelectata >= 0 && formaSelectata < forme.size()) {
                                salveazaStarePentruUndo();
                                forme.get(formaSelectata).aplicaRotatie(unghi);
                                repaint();
                        } else {
                                JOptionPane.showMessageDialog(JavaSwingPaintv4.this, "Selectați mai întâi o formă dând click pe ea!");
                        }
		}

                public void aplicaScalareFormaSelectata(double sx, double sy) {
                        if (formaSelectata >= 0 && formaSelectata < forme.size()) {
                                salveazaStarePentruUndo();
                                forme.get(formaSelectata).aplicaScalare(sx, sy);
                                repaint();
                        } else {
                                JOptionPane.showMessageDialog(JavaSwingPaintv4.this, "Selectați mai întâi o formă dând click pe ea!");
                        }
		}

                public void aplicaInclinareFormaSelectata(double shx, double shy) {
                        if (formaSelectata >= 0 && formaSelectata < forme.size()) {
                                salveazaStarePentruUndo();
                                forme.get(formaSelectata).aplicaInclinare(shx, shy);
                                repaint();
                        } else {
                                JOptionPane.showMessageDialog(JavaSwingPaintv4.this, "Selectați mai întâi o formă dând click pe ea!");
                        }
		}

		public void aplicaOperatieArie(String operatie) {
			// 1) Cazul preferat: avem două forme selectate explicit
			if (formaSelectata >= 0 && formaSelectata2 >= 0 && formaSelectata < forme.size()
					&& formaSelectata2 < forme.size() && formaSelectata != formaSelectata2) {

				int a = formaSelectata; // PRIMA formă selectată -> area1 (important pentru SUBTRACT)
				int b = formaSelectata2; // A DOUA formă selectată -> area2

				Area area1 = forme.get(a).getArea();
				Area area2 = forme.get(b).getArea();

                                if (area1 != null && area2 != null) {
                                        switch (operatie) {
                                        case "REUNIUNE":
                                                area1.add(area2);
                                                break;
                                        case "EXTRAGERE":
                                                area1.subtract(area2);
                                                break; // area1 - area2
                                        case "INTERSECTIE":
                                                area1.intersect(area2);
                                                break;
                                        case "XOR":
                                                area1.exclusiveOr(area2);
                                                break;
                                        }

                                        salveazaStarePentruUndo();
                                        Forma formaNoua = new Forma(area1, culoareCurenta, culoareGradient, dash, dashPhase, grosimeLinie,
                                                        tipUmplere, texturaImagine);

                                        // păstrăm rezultatul în locul PRIMEI forme selectate
                                        if (a < b) {
						forme.set(a, formaNoua);
						forme.remove(b);
						formaSelectata = a;
					} else { // b < a
						forme.remove(b); // după remove, indexul 'a' scade cu 1
						forme.set(a - 1, formaNoua);
						formaSelectata = a - 1;
					}
					formaSelectata2 = -1; // am terminat perechea
					repaint();
					return;
				} else {
					JOptionPane.showMessageDialog(JavaSwingPaintv4.this,
							"Operațiile cu arii funcționează doar pentru forme închise!");
					return;
				}
			}

			// 2) Fallback: vechiul comportament (prima + următoarea din listă)
			if (formaSelectata >= 0 && formaSelectata < forme.size() - 1) {
				Forma forma1 = forme.get(formaSelectata);
				Forma forma2 = forme.get(formaSelectata + 1);

				Area area1 = forma1.getArea();
				Area area2 = forma2.getArea();

                                if (area1 != null && area2 != null) {
                                        switch (operatie) {
                                        case "REUNIUNE":
                                                area1.add(area2);
                                                break;
                                        case "EXTRAGERE":
                                                area1.subtract(area2);
                                                break;
                                        case "INTERSECTIE":
                                                area1.intersect(area2);
                                                break;
                                        case "XOR":
                                                area1.exclusiveOr(area2);
                                                break;
                                        }

                                        salveazaStarePentruUndo();
                                        Forma formaNoua = new Forma(area1, culoareCurenta, culoareGradient, dash, dashPhase, grosimeLinie,
                                                        tipUmplere, texturaImagine);
					forme.set(formaSelectata, formaNoua);
					forme.remove(formaSelectata + 1);
					formaSelectata2 = -1;
					repaint();
				} else {
					JOptionPane.showMessageDialog(JavaSwingPaintv4.this,
							"Operațiile cu arii funcționează doar pentru forme închise!");
				}
			} else {
				JOptionPane.showMessageDialog(JavaSwingPaintv4.this,
						"Selectați două forme (sau selectați o formă urmată imediat de alta în listă).");
			}
		}

                private void finalizeazaPoligon() {
                        if (punctePoligon.size() >= 3) {
                                Forma forma = new Forma("POLIGON", culoareCurenta, culoareGradient, punctePoligon, fontCurent, dash,
                                                dashPhase, grosimeLinie, tipUmplere, texturaImagine);
                                salveazaStarePentruUndo();
                                forme.add(forma);
                        }
                        punctePoligon.clear();
                        repaint();
                }

		private void selecteazaImagine(Point pozitie) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Imagini (*.jpg, *.png, *.gif)", "jpg", "jpeg",
					"png", "gif");
			fileChooser.setFileFilter(filter);

			int result = fileChooser.showOpenDialog(JavaSwingPaintv4.this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File fisier = fileChooser.getSelectedFile();
				ImageIcon icon = new ImageIcon(fisier.getAbsolutePath());
                                Forma forma = new Forma("IMAGINE", culoareCurenta, culoareGradient, pozitie,
                                                new Point(pozitie.x + 100, pozitie.y + 100), null, null, fontCurent, dash, dashPhase,
                                                grosimeLinie, tipUmplere, texturaImagine);
                                forma.setImagine(icon.getImage());
                                salveazaStarePentruUndo();
                                forme.add(forma);
                                repaint();
                        }
                }

		private void adaugaText(final Point pozitie) {
			final JDialog dialog = new JDialog(JavaSwingPaintv4.this, "Selectie Font și Text", true);
			dialog.setLayout(new GridLayout(5, 2, 5, 5));

			String[] fonturi = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
			final JComboBox<String> listaFonturi = new JComboBox<>(fonturi);

			final JComboBox<String> listaStil = new JComboBox<>(
					new String[] { "Plain", "Bold", "Italic", "Bold+Italic" });

			Integer[] dimensiuni = { 10, 12, 14, 16, 18, 20, 24, 28, 32, 36, 40, 48, 56, 64, 72 };
			final JComboBox<Integer> listaDimensiune = new JComboBox<>(dimensiuni);
			listaDimensiune.setSelectedItem(24);

			final JTextField campText = new JTextField("Text exemplu");

			JButton butonOK = new JButton("OK");
			JButton butonAnuleaza = new JButton("Anulează");

			butonOK.addActionListener(e -> {
				String font = (String) listaFonturi.getSelectedItem();
				String stilText = (String) listaStil.getSelectedItem();
				int stil = Font.PLAIN;

				if ("Bold".equals(stilText))
					stil = Font.BOLD;
				else if ("Italic".equals(stilText))
					stil = Font.ITALIC;
				else if ("Bold+Italic".equals(stilText))
					stil = Font.BOLD | Font.ITALIC;

				int dimensiune = (Integer) listaDimensiune.getSelectedItem();
				String text = campText.getText();

                                if (text != null && !text.isEmpty()) {
                                        Font fontNou = new Font(font, stil, dimensiune);
                                        Forma forma = new Forma("TEXT", culoareCurenta, culoareGradient, pozitie, pozitie, null, null,
                                                        fontNou, dash, dashPhase, grosimeLinie, tipUmplere, texturaImagine);
                                        forma.setText(text);
                                        salveazaStarePentruUndo();
                                        forme.add(forma);
                                        repaint();
                                }
                                dialog.dispose();
                        });

			butonAnuleaza.addActionListener(e -> dialog.dispose());

			dialog.add(new JLabel("Font:"));
			dialog.add(listaFonturi);
			dialog.add(new JLabel("Stil:"));
			dialog.add(listaStil);
			dialog.add(new JLabel("Dimensiune:"));
			dialog.add(listaDimensiune);
			dialog.add(new JLabel("Text:"));
			dialog.add(campText);
			dialog.add(butonOK);
			dialog.add(butonAnuleaza);

			dialog.pack();
			dialog.setLocationRelativeTo(JavaSwingPaintv4.this);
			dialog.setVisible(true);
		}

		private void adaugaTextForma(final Point pozitie) {
			final JDialog dialog = new JDialog(JavaSwingPaintv4.this, "Text ca Formă", true);
			dialog.setLayout(new GridLayout(5, 2, 5, 5));

			String[] fonturi = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
			final JComboBox<String> listaFonturi = new JComboBox<>(fonturi);

			final JComboBox<String> listaStil = new JComboBox<>(
					new String[] { "Plain", "Bold", "Italic", "Bold+Italic" });

			Integer[] dimensiuni = { 20, 24, 28, 32, 36, 40, 48, 56, 64, 72, 80, 96 };
			final JComboBox<Integer> listaDimensiune = new JComboBox<>(dimensiuni);
			listaDimensiune.setSelectedItem(48);

			final JTextField campText = new JTextField("TEXT");

			JButton butonOK = new JButton("OK");
			JButton butonAnuleaza = new JButton("Anulează");

			butonOK.addActionListener(e -> {
				String font = (String) listaFonturi.getSelectedItem();
				String stilText = (String) listaStil.getSelectedItem();
				int stil = Font.PLAIN;

				if ("Bold".equals(stilText))
					stil = Font.BOLD;
				else if ("Italic".equals(stilText))
					stil = Font.ITALIC;
				else if ("Bold+Italic".equals(stilText))
					stil = Font.BOLD | Font.ITALIC;

				int dimensiune = (Integer) listaDimensiune.getSelectedItem();
				String text = campText.getText();

                                if (text != null && !text.isEmpty()) {
                                        Font fontNou = new Font(font, stil, dimensiune);
                                        Forma forma = new Forma("TEXT_FORMA", culoareCurenta, culoareGradient, pozitie, pozitie, null, null,
                                                        fontNou, dash, dashPhase, grosimeLinie, tipUmplere, texturaImagine);
                                        forma.setText(text);
                                        salveazaStarePentruUndo();
                                        forme.add(forma);
                                        repaint();
                                }
                                dialog.dispose();
                        });

			butonAnuleaza.addActionListener(e -> dialog.dispose());

			dialog.add(new JLabel("Font:"));
			dialog.add(listaFonturi);
			dialog.add(new JLabel("Stil:"));
			dialog.add(listaStil);
			dialog.add(new JLabel("Dimensiune:"));
			dialog.add(listaDimensiune);
			dialog.add(new JLabel("Text:"));
			dialog.add(campText);
			dialog.add(butonOK);
			dialog.add(butonAnuleaza);

			dialog.pack();
			dialog.setLocationRelativeTo(JavaSwingPaintv4.this);
			dialog.setVisible(true);
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;

			// Setează antialiasing pentru calitate superioară
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// Aplică zona de decupare dacă există
			if (zonaDecupare != null) {
				g2d.setClip(zonaDecupare);
			}

			// Desenează toate formele
			for (int i = 0; i < forme.size(); i++) {
				Forma forma = forme.get(i);
				forma.deseneaza(g2d);

				// Evidențiază forma selectată
				if (i == formaSelectata || i == formaSelectata2) {
					Rectangle2D bounds = forma.getBounds();
					if (bounds != null) {
						g2d.setColor(i == formaSelectata ? Color.BLUE : new Color(255, 140, 0)); // albastru pt prima,
																									// portocaliu pt a
																									// doua
						g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10,
								new float[] { 5, 5 }, 0));
						g2d.draw(bounds);
					}
				}
			}

			// Desenează forma în curs de desenare
			if (punctStart != null && punctCurent != null && !modDecupare) {
				g2d.setColor(culoareCurenta);
				Stroke stroke = creeazaStrokeCustom();
				g2d.setStroke(stroke);
				deseneazaPrevizualizare(g2d, primitivaCurenta, punctStart, punctCurent);
			}

			// Desenează poligonul în curs de creare
			if (punctePoligon.size() > 0) {
				g2d.setColor(culoareCurenta);
				Stroke stroke = creeazaStrokeCustom();
				g2d.setStroke(stroke);
				for (int i = 0; i < punctePoligon.size() - 1; i++) {
					Point p1 = punctePoligon.get(i);
					Point p2 = punctePoligon.get(i + 1);
					g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
				}
				for (Point p : punctePoligon) {
					g2d.fillOval(p.x - 3, p.y - 3, 6, 6);
				}
			}

			// Desenează punctele de control pentru curbe
			if (etapaCurba > 0 && punctStart != null) {
				g2d.setColor(Color.RED);
				g2d.fillOval(punctStart.x - 4, punctStart.y - 4, 8, 8);
				if (punctControl1 != null) {
					g2d.setColor(Color.GREEN);
					g2d.fillOval(punctControl1.x - 4, punctControl1.y - 4, 8, 8);
					g2d.setColor(Color.GRAY);
					g2d.drawLine(punctStart.x, punctStart.y, punctControl1.x, punctControl1.y);
				}
				if (punctControl2 != null) {
					g2d.setColor(Color.BLUE);
					g2d.fillOval(punctControl2.x - 4, punctControl2.y - 4, 8, 8);
					g2d.setColor(Color.GRAY);
					g2d.drawLine(punctControl1.x, punctControl1.y, punctControl2.x, punctControl2.y);
				}
			}

			// Desenează dreptunghiul pentru decupare
			if (modDecupare && punctStart != null && punctCurent != null) {
				g2d.setColor(new Color(0, 0, 255, 100));
				int x = Math.min(punctStart.x, punctCurent.x);
				int y = Math.min(punctStart.y, punctCurent.y);
				int width = Math.abs(punctCurent.x - punctStart.x);
				int height = Math.abs(punctCurent.y - punctStart.y);
				g2d.fill(new Rectangle2D.Double(x, y, width, height));
				g2d.setColor(Color.BLUE);
				g2d.setStroke(new BasicStroke(2));
				g2d.draw(new Rectangle2D.Double(x, y, width, height));
			}

			// Resetează clip-ul
			if (zonaDecupare != null) {
				g2d.setClip(null);
				g2d.setColor(Color.RED);
				g2d.setStroke(
						new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] { 10, 5 }, 0));
				g2d.draw(zonaDecupare);
			}
		}

		private void deseneazaPrevizualizare(Graphics2D g2d, String tip, Point p1, Point p2) {
			int x = Math.min(p1.x, p2.x);
			int y = Math.min(p1.y, p2.y);
			int width = Math.abs(p2.x - p1.x);
			int height = Math.abs(p2.y - p1.y);

			switch (tip) {
			case "LINIE":
				g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
				break;
			case "DREPTUNGHI":
				g2d.draw(new Rectangle2D.Double(x, y, width, height));
				break;
			case "DREPTUNGHI_ROTUND":
				g2d.draw(new RoundRectangle2D.Double(x, y, width, height, 20, 20));
				break;
			case "ELIPSA":
				g2d.draw(new Ellipse2D.Double(x, y, width, height));
				break;
			case "ARC_ELIPSA":
				g2d.draw(new Arc2D.Double(x, y, width, height, 30, 120, Arc2D.OPEN));
				break;
			case "SEGMENT_ELIPSA":
				g2d.draw(new Arc2D.Double(x, y, width, height, 30, 120, Arc2D.CHORD));
				break;
			case "SECTOR_ELIPSA":
				g2d.draw(new Arc2D.Double(x, y, width, height, 30, 120, Arc2D.PIE));
				break;
			}
		}
	}

        class Forma {
                private String tip;
                private Color culoare, culoareGradient;
                private Point punct1, punct2, punctControl1, punctControl2;
                private ArrayList<Point> puncte;
                private Image imagine;
		private String text;
		private Font font;
		private int grosimeLinie, tipUmplere;

		private float[] dash;
		private float dashPhase;

		private Image texturaImagine;
                private Shape shape;
                private AffineTransform transform;
                private Area area;

                public Forma(Forma other) {
                        this.tip = other.tip;
                        this.culoare = other.culoare;
                        this.culoareGradient = other.culoareGradient;
                        this.punct1 = other.punct1 != null ? new Point(other.punct1) : null;
                        this.punct2 = other.punct2 != null ? new Point(other.punct2) : null;
                        this.punctControl1 = other.punctControl1 != null ? new Point(other.punctControl1) : null;
                        this.punctControl2 = other.punctControl2 != null ? new Point(other.punctControl2) : null;
                        if (other.puncte != null) {
                                this.puncte = new ArrayList<>();
                                for (Point p : other.puncte) {
                                        this.puncte.add(new Point(p));
                                }
                        } else {
                                this.puncte = null;
                        }
                        this.imagine = other.imagine;
                        this.text = other.text;
                        this.font = other.font;
                        this.grosimeLinie = other.grosimeLinie;
                        this.tipUmplere = other.tipUmplere;
                        this.dash = other.dash != null ? other.dash.clone() : null;
                        this.dashPhase = other.dashPhase;
                        this.texturaImagine = other.texturaImagine;
                        this.transform = new AffineTransform(other.transform);
                        this.area = other.area != null ? (Area) other.area.clone() : null;

                        if ("AREA".equals(this.tip)) {
                                this.shape = this.area != null ? new Area(this.area) : null;
                        } else {
                                this.shape = null;
                                creeazaShape();
                                if ("IMAGINE".equals(this.tip)) {
                                        setImagine(this.imagine);
                                }
                                if ("TEXT".equals(this.tip) || "TEXT_FORMA".equals(this.tip)) {
                                        setText(this.text);
                                }
                                if (this.area == null && other.area != null) {
                                        this.area = (Area) other.area.clone();
                                }
                        }
                }

                public Forma(String tip, Color culoare, Color culoareGradient, Point p1, Point p2, Point pCtrl1, Point pCtrl2,
                                Font font, float[] dash, float dashPhase, int grosimeLinie, int tipUmplere, Image texturaImagine) {
                        this.tip = tip;
                        this.culoare = culoare;
                        this.culoareGradient = culoareGradient;
			this.punct1 = p1;
			this.punct2 = p2;
			this.punctControl1 = pCtrl1;
			this.punctControl2 = pCtrl2;
			this.font = font;

			this.dash = dash;
			this.dashPhase = dashPhase;

			this.grosimeLinie = grosimeLinie;
			this.tipUmplere = tipUmplere;
			this.texturaImagine = texturaImagine;
			this.transform = new AffineTransform();
			this.puncte = null;
			this.imagine = null;
			this.text = null;
			creeazaShape();
		}

                public Forma(String tip, Color culoare, Color culoareGradient, ArrayList<Point> puncte, Font font, float[] dash,
                                float dashPhase, int grosimeLinie, int tipUmplere, Image texturaImagine) {
			this.tip = tip;
			this.culoare = culoare;
			this.culoareGradient = culoareGradient;
			this.puncte = new ArrayList<>(puncte);
			this.font = font;

			this.dash = dash;
			this.dashPhase = dashPhase;

			this.grosimeLinie = grosimeLinie;
			this.tipUmplere = tipUmplere;
			this.texturaImagine = texturaImagine;
			this.transform = new AffineTransform();
			this.punct1 = null;
			this.punct2 = null;
			this.imagine = null;
			this.text = null;
			creeazaShape();
		}

		// Constructor pentru Area compusă
                public Forma(Area area, Color culoare, Color culoareGradient, float[] dash, float dashPhase, int grosimeLinie,
                                int tipUmplere, Image texturaImagine) {
                        this.tip = "AREA";
			this.culoare = culoare;
			this.culoareGradient = culoareGradient;
			this.area = area;
			this.shape = area;

			this.dash = dash;
			this.dashPhase = dashPhase;

			this.grosimeLinie = grosimeLinie;
			this.tipUmplere = tipUmplere;
			this.texturaImagine = texturaImagine;
			this.transform = new AffineTransform();
		}

                public Forma(FormaSerializata serializata) throws IOException {
                        this.tip = serializata.tip;
                        this.culoare = serializata.culoare;
                        this.culoareGradient = serializata.culoareGradient;
                        this.punct1 = serializata.punct1 != null ? new Point(serializata.punct1) : null;
                        this.punct2 = serializata.punct2 != null ? new Point(serializata.punct2) : null;
                        this.punctControl1 = serializata.punctControl1 != null ? new Point(serializata.punctControl1) : null;
                        this.punctControl2 = serializata.punctControl2 != null ? new Point(serializata.punctControl2) : null;
                        if (serializata.puncte != null) {
                                this.puncte = new ArrayList<>();
                                for (Point p : serializata.puncte) {
                                        this.puncte.add(new Point(p));
                                }
                        }
                        this.font = serializata.font;
                        this.dash = serializata.dash != null ? serializata.dash.clone() : null;
                        this.dashPhase = serializata.dashPhase;
                        this.grosimeLinie = serializata.grosimeLinie;
                        this.tipUmplere = serializata.tipUmplere;
                        this.transform = serializata.transform != null ? new AffineTransform(serializata.transform)
                                        : new AffineTransform();
                        this.text = serializata.text;
                        this.texturaImagine = imagineDinOcteti(serializata.texturaImagine);

                        if ("AREA".equals(this.tip)) {
                                Path2D.Double path = serializata.areaPath;
                                if (path != null) {
                                        this.area = new Area(path);
                                        this.shape = path;
                                }
                        } else {
                                creeazaShape();
                        }

                        if ("IMAGINE".equals(this.tip)) {
                                Image img = imagineDinOcteti(serializata.imagine);
                                if (img != null) {
                                        setImagine(img);
                                }
                        } else {
                                this.imagine = null;
                        }

                        if (this.text != null && ("TEXT".equals(this.tip) || "TEXT_FORMA".equals(this.tip))) {
                                setText(this.text);
                        }
                }

                private void creeazaShape() {
			if (puncte != null) {
				// Creează poligon
				GeneralPath path = new GeneralPath();
				path.moveTo(puncte.get(0).x, puncte.get(0).y);
				for (int i = 1; i < puncte.size(); i++) {
					path.lineTo(puncte.get(i).x, puncte.get(i).y);
				}
				path.closePath();
				shape = path;
				area = new Area(shape);
			} else if (punct1 != null && punct2 != null) {
				int x = Math.min(punct1.x, punct2.x);
				int y = Math.min(punct1.y, punct2.y);
				int width = Math.abs(punct2.x - punct1.x);
				int height = Math.abs(punct2.y - punct1.y);

				switch (tip) {
				case "LINIE":
					shape = new Line2D.Double(punct1.x, punct1.y, punct2.x, punct2.y);
					break;
				case "DREPTUNGHI":
					shape = new Rectangle2D.Double(x, y, width, height);
					area = new Area(shape);
					break;
				case "DREPTUNGHI_ROTUND":
					shape = new RoundRectangle2D.Double(x, y, width, height, 20, 20);
					area = new Area(shape);
					break;
				case "ELIPSA":
					shape = new Ellipse2D.Double(x, y, width, height);
					area = new Area(shape);
					break;
				case "ARC_ELIPSA":
					shape = new Arc2D.Double(x, y, width, height, 30, 120, Arc2D.OPEN);
					break;
				case "SEGMENT_ELIPSA":
					shape = new Arc2D.Double(x, y, width, height, 30, 120, Arc2D.CHORD);
					area = new Area(shape);
					break;
				case "SECTOR_ELIPSA":
					shape = new Arc2D.Double(x, y, width, height, 30, 120, Arc2D.PIE);
					area = new Area(shape);
					break;
				case "CURBA_PATRATA":
					if (punctControl1 != null) {
						GeneralPath path = new GeneralPath();
						path.moveTo(punct1.x, punct1.y);
						path.quadTo(punctControl1.x, punctControl1.y, punct2.x, punct2.y);
						shape = path;
					}
					break;
				case "CURBA_CUBICA":
					if (punctControl1 != null && punctControl2 != null) {
						GeneralPath path = new GeneralPath();
						path.moveTo(punct1.x, punct1.y);
						path.curveTo(punctControl1.x, punctControl1.y, punctControl2.x, punctControl2.y, punct2.x,
								punct2.y);
						shape = path;
					}
					break;
				case "IMAGINE":
					// dacă nu avem încă imaginea, folosim dreptunghiul trasat (p1..p2)
					int w = Math.abs(punct2.x - punct1.x);
					int h = Math.abs(punct2.y - punct1.y);
					if (w <= 0)
						w = 100;
					if (h <= 0)
						h = 100;
					shape = new Rectangle2D.Double(x, y, w, h);
					// area rămâne null ca să nu fie eligibilă pentru operații de arii
					break;
				}
			}
		}


		public void setCuloare(Color culoare) {
			this.culoare = culoare;
		}

		public void setCuloareGradient(Color culoareGradient) {
			this.culoareGradient = culoareGradient;
		}

		public void setGrosimeLinie(int grosimeLinie) {
			this.grosimeLinie = grosimeLinie;
		}

		public void setTipUmplere(int tipUmplere) {
			this.tipUmplere = tipUmplere;
		}

		public void setDashPattern(float[] dash, float dashPhase) {
			this.dash = dash != null ? dash.clone() : null;
			this.dashPhase = dashPhase;
		}

		public void setTexturaImagine(Image texturaImagine) {
			this.texturaImagine = texturaImagine;
		}

		public void setImagine(Image imagine) {
			this.imagine = imagine;

			// >>> adaugă: definește shape-ul imaginii ca să poată fi selectată
			int width = Math.abs(punct2.x - punct1.x);
			int height = Math.abs(punct2.y - punct1.y);

			if (imagine != null) {
				if (width == 0)
					width = imagine.getWidth(null);
				if (height == 0)
					height = imagine.getHeight(null);
			}
			if (width <= 0)
				width = 100; // fallback
			if (height <= 0)
				height = 100; // fallback

			int x = Math.min(punct1.x, punct2.x);
			int y = Math.min(punct1.y, punct2.y);

			this.shape = new Rectangle2D.Double(x, y, width, height);
			this.area = null; // imaginile nu participă la operații de arii
			// <<< adaugă
		}

                public void setText(String text) {
                        this.text = text;
                        if (tip.equals("TEXT_FORMA") && font != null) {
                                // Creează shape din text
                                FontRenderContext frc = new FontRenderContext(null, true, true);
                                TextLayout layout = new TextLayout(text, font, frc);
                                AffineTransform at = new AffineTransform();
                                at.translate(punct1.x, punct1.y + layout.getBounds().getHeight());
                                shape = layout.getOutline(at);
                                area = new Area(shape);
                        }
                }

                public FormaSerializata toSerializat() throws IOException {
                        FormaSerializata serializata = new FormaSerializata();
                        serializata.tip = this.tip;
                        serializata.culoare = this.culoare;
                        serializata.culoareGradient = this.culoareGradient;
                        serializata.punct1 = this.punct1 != null ? new Point(this.punct1) : null;
                        serializata.punct2 = this.punct2 != null ? new Point(this.punct2) : null;
                        serializata.punctControl1 = this.punctControl1 != null ? new Point(this.punctControl1) : null;
                        serializata.punctControl2 = this.punctControl2 != null ? new Point(this.punctControl2) : null;
                        if (this.puncte != null) {
                                serializata.puncte = new ArrayList<>();
                                for (Point p : this.puncte) {
                                        serializata.puncte.add(new Point(p));
                                }
                        }
                        serializata.font = this.font;
                        serializata.dash = this.dash != null ? this.dash.clone() : null;
                        serializata.dashPhase = this.dashPhase;
                        serializata.grosimeLinie = this.grosimeLinie;
                        serializata.tipUmplere = this.tipUmplere;
                        serializata.transform = new AffineTransform(this.transform);
                        serializata.text = this.text;
                        serializata.texturaImagine = imagineInOcteti(this.texturaImagine);
                        serializata.imagine = imagineInOcteti(this.imagine);
                        if ("AREA".equals(this.tip) && this.area != null) {
                                serializata.areaPath = creeazaPathDinShape(this.area);
                        }
                        return serializata;
                }

                private Path2D.Double creeazaPathDinShape(Shape forma) {
                        if (forma == null) {
                                return null;
                        }
                        PathIterator iterator = forma.getPathIterator(null);
                        Path2D.Double path = new Path2D.Double();
                        double[] coords = new double[6];
                        while (!iterator.isDone()) {
                                switch (iterator.currentSegment(coords)) {
                                case PathIterator.SEG_MOVETO:
                                        path.moveTo(coords[0], coords[1]);
                                        break;
                                case PathIterator.SEG_LINETO:
                                        path.lineTo(coords[0], coords[1]);
                                        break;
                                case PathIterator.SEG_QUADTO:
                                        path.quadTo(coords[0], coords[1], coords[2], coords[3]);
                                        break;
                                case PathIterator.SEG_CUBICTO:
                                        path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                                        break;
                                case PathIterator.SEG_CLOSE:
                                        path.closePath();
                                        break;
                                }
                                iterator.next();
                        }
                        return path;
                }

                private byte[] imagineInOcteti(Image image) throws IOException {
                        if (image == null) {
                                return null;
                        }
                        BufferedImage buffered = asBufferedImage(image);
                        if (buffered == null) {
                                return null;
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(buffered, "png", baos);
                        return baos.toByteArray();
                }

                private BufferedImage asBufferedImage(Image image) {
                        if (image == null) {
                                return null;
                        }
                        if (image instanceof BufferedImage) {
                                return (BufferedImage) image;
                        }
                        int width = image.getWidth(null);
                        int height = image.getHeight(null);
                        if (width <= 0 || height <= 0) {
                                return null;
                        }
                        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2 = bufferedImage.createGraphics();
                        g2.drawImage(image, 0, 0, null);
                        g2.dispose();
                        return bufferedImage;
                }

                private Image imagineDinOcteti(byte[] data) throws IOException {
                        if (data == null || data.length == 0) {
                                return null;
                        }
                        ByteArrayInputStream bais = new ByteArrayInputStream(data);
                        return ImageIO.read(bais);
                }

                public boolean contine(Point p) {
                        if (shape != null) {
                                Shape transformedShape = transform.createTransformedShape(shape);
				if (transformedShape != null) {
					return transformedShape.contains(p);
				}
			}
			return false;
		}

		public Rectangle2D getBounds() {
			if (shape != null) {
				Shape transformedShape = transform.createTransformedShape(shape);
				if (transformedShape != null) {
					return transformedShape.getBounds2D();
				}
			}
			return null;
		}

		public Area getArea() {
			if (area != null) {
				Area transformedArea = (Area) area.clone();
				transformedArea.transform(transform);
				return transformedArea;
			}
			return null;
		}

		public void aplicaTransformare(AffineTransform at) {
			transform.concatenate(at);
		}

		public void aplicaRotatie(double unghi) {
			Rectangle2D bounds = getBounds();
			if (bounds != null) {
				double cx = bounds.getCenterX();
				double cy = bounds.getCenterY();
				AffineTransform at = AffineTransform.getRotateInstance(unghi, cx, cy);
				transform.concatenate(at);
			}
		}

		public void aplicaScalare(double sx, double sy) {
			Rectangle2D bounds = getBounds();
			if (bounds != null) {
				double cx = bounds.getCenterX();
				double cy = bounds.getCenterY();
				AffineTransform at = new AffineTransform();
				at.translate(cx, cy);
				at.scale(sx, sy);
				at.translate(-cx, -cy);
				transform.concatenate(at);
			}
		}

		public void aplicaInclinare(double shx, double shy) {
			AffineTransform at = AffineTransform.getShearInstance(shx, shy);
			transform.concatenate(at);
		}

		public void deseneaza(Graphics2D g2d) {
			Graphics2D g = (Graphics2D) g2d.create();

			// Aplică transformarea
			g.transform(transform);

			// Setează stilul liniei
			Stroke stroke = creeazaStroke();
			g.setStroke(stroke);

			if (imagine != null && tip.equals("IMAGINE")) {
				int width = Math.abs(punct2.x - punct1.x);
				int height = Math.abs(punct2.y - punct1.y);
				if (width == 0)
					width = imagine.getWidth(null);
				if (height == 0)
					height = imagine.getHeight(null);
				g.drawImage(imagine, punct1.x, punct1.y, width, height, null);
			} else if (text != null && tip.equals("TEXT")) {
				g.setFont(font);
				g.setColor(culoare);
				g.drawString(text, punct1.x, punct1.y);
			} else if (shape != null) {
				// Aplică umplerea
				if (tipUmplere > 0 && (area != null || tip.equals("TEXT_FORMA"))) {
					Paint paint = creeazaPaint();
					if (paint != null) {
						g.setPaint(paint);
						g.fill(shape);
					}
				}

				// Desenează conturul
				g.setColor(culoare);
				g.draw(shape);
			}

			g.dispose();
		}

		private Stroke creeazaStroke() {
			if (dash != null && dash.length > 0) {
				return new BasicStroke(grosimeLinie, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash,
						dashPhase);
			}
			return new BasicStroke(grosimeLinie);
		}

		private Paint creeazaPaint() {
			Rectangle2D bounds = shape.getBounds2D();
			if (bounds.isEmpty())
				return null;

			switch (tipUmplere) {
			case 1: // Culoare solidă
				return culoare;

			case 2: // Gradient aciclic
				return new GradientPaint((float) bounds.getMinX(), (float) bounds.getMinY(), culoare,
						(float) bounds.getMaxX(), (float) bounds.getMaxY(), culoareGradient, false);

			case 3: // Gradient ciclic
				float cx = (float) bounds.getCenterX();
				float cy = (float) bounds.getCenterY();
				float width = (float) bounds.getWidth();
				float height = (float) bounds.getHeight();
				float dim = Math.min(width, height) / 4;
				return new GradientPaint(cx - dim, cy - dim, culoare, cx + dim, cy + dim, culoareGradient, true);

			case 4: // Textură desen (cerc)
				BufferedImage bimg = new BufferedImage(15, 15, BufferedImage.TYPE_INT_RGB);
				Graphics2D bg2d = bimg.createGraphics();
				Rectangle suport = new Rectangle(0, 0, 15, 15);
				Ellipse2D cerc = new Ellipse2D.Float(2, 2, 11, 11);
				bg2d.setColor(Color.WHITE);
				bg2d.fill(suport);
				bg2d.setColor(new Color(culoare.getRed(), culoare.getGreen(), culoare.getBlue(), 128));
				bg2d.fill(cerc);
				bg2d.setColor(culoare);
				bg2d.draw(cerc);
				bg2d.dispose();
				return new TexturePaint(bimg, suport);

			case 5: // Textură imagine
				if (texturaImagine != null) {
					int imgW = texturaImagine.getWidth(null);
					int imgH = texturaImagine.getHeight(null);
					if (imgW > 0 && imgH > 0) {
						// Limitează dimensiunea texturii
						int maxDim = 50;
						if (imgW > maxDim || imgH > maxDim) {
							float scale = Math.min((float) maxDim / imgW, (float) maxDim / imgH);
							imgW = (int) (imgW * scale);
							imgH = (int) (imgH * scale);
						}

						BufferedImage scaled = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
						Graphics2D g2 = scaled.createGraphics();
						g2.drawImage(texturaImagine, 0, 0, imgW, imgH, null);
						g2.dispose();

						Rectangle2D anchor = new Rectangle2D.Double(0, 0, imgW, imgH);
						return new TexturePaint(scaled, anchor);
					}
				}
				return culoare;

			default:
				return null;
			}
		}
	}

        public static void main(String[] args) {
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                                new JavaSwingPaintv4();
                        }
                });
        }

        private static class DesenSerializat implements Serializable {
                private static final long serialVersionUID = 1L;

                private ArrayList<FormaSerializata> forme;
                private RectangleData zonaDecupare;
        }

        private static class RectangleData implements Serializable {
                private static final long serialVersionUID = 1L;

                private double x;
                private double y;
                private double latime;
                private double inaltime;

                private RectangleData(double x, double y, double latime, double inaltime) {
                        this.x = x;
                        this.y = y;
                        this.latime = latime;
                        this.inaltime = inaltime;
                }

                private static RectangleData fromShape(Shape shape) {
                        if (shape instanceof Rectangle2D) {
                                Rectangle2D rect = (Rectangle2D) shape;
                                return new RectangleData(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
                        }
                        return null;
                }

                private Shape toShape() {
                        return new Rectangle2D.Double(x, y, latime, inaltime);
                }
        }

        private static class FormaSerializata implements Serializable {
                private static final long serialVersionUID = 1L;

                private String tip;
                private Color culoare;
                private Color culoareGradient;
                private Point punct1;
                private Point punct2;
                private Point punctControl1;
                private Point punctControl2;
                private ArrayList<Point> puncte;
                private String text;
                private Font font;
                private int grosimeLinie;
                private int tipUmplere;
                private float[] dash;
                private float dashPhase;
                private AffineTransform transform;
                private byte[] imagine;
                private byte[] texturaImagine;
                private Path2D.Double areaPath;
        }
}
