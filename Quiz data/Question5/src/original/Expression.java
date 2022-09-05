package original;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Expression {
    private String op;
    private int leftValue;
    private int rightValue;
    private boolean isUnary;
    private Expression left;
    private Expression right;

    public Expression(int leftValue, String op, int rightValue) {
        if (op != "+"
                && op != "-"
                && op != "*"
                && op != "/") {
            throw new IllegalArgumentException("Invalid operator: "
                    + op
                    + ".");
        }
        this.leftValue = leftValue;
        this.op = op;
        this.rightValue = rightValue;
    }

    public Expression(Expression left, String op, Expression right) {
        if (op != "+"
                && op != "-"
                && op != "*"
                && op != "/") {
            throw new IllegalArgumentException("Invalid operator: "
                    + op
                    + ".");
        }
        if (left == null) {
            throw new IllegalArgumentException("left is null");
        }
        if (right == null) {
            throw new IllegalArgumentException("right is null");
        }
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public Expression(int leftValue) {
        this.leftValue = leftValue;
        isUnary = true;
    }

    public int Evaluate() {
        if (isUnary) {
            return leftValue;
        } else if (left != null
                && right != null) {
            switch (op) {
            case "+":
                return left.Evaluate()
                        + right.Evaluate();
            case "-":
                return left.Evaluate()
                        - right.Evaluate();
            case "*":
                return left.Evaluate()
                        * right.Evaluate();
            case "/":
                int rightEvaluatedValue = right.Evaluate();
                if (rightEvaluatedValue == 0) {
                    throw new ArithmeticException("Cannot divide by 0.");
                }
                return left.Evaluate()
                        / right.Evaluate();
            }
        } else if (left == null
                && right == null) {
            switch (op) {
            case "+":
                return leftValue
                        + rightValue;
            case "-":
                return leftValue
                        - rightValue;
            case "*":
                return leftValue
                        * rightValue;
            case "/":
                if (rightValue == 0) {
                    throw new ArithmeticException("Cannot divide by 0.");
                }
                return leftValue
                        / rightValue;
            }
        }

        // We should never get here
        System.out.println("Invalid state!");
        return 0;
    }

    public String PrintString() {
        if (isUnary) {
            return Integer.toString(leftValue);
        } else if (left != null
                && right != null) {
            return "("
                    + left.PrintString()
                    + " "
                    + op
                    + " "
                    + right.PrintString()
                    + ")";
        } else if (left == null
                && right == null) {
            return "("
                    + leftValue
                    + " "
                    + op
                    + " "
                    + rightValue
                    + ")";
        } else {
            // We should never get here
            throw new IllegalStateException("Invalid state!");
        }
    }

    public boolean SerializeToXml(String path) {
        File outFile = new File(path);
        OutputStreamWriter writer;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(outFile));
            // Create document factory
            DocumentBuilderFactory docFact = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder docBuilder;
            // Build document
            docBuilder = docFact.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            // Create root node
            Element root = doc.createElement("root");
            doc.appendChild(root);

            Element xml = GetXml(doc);
            // Append xml content to root node
            root.appendChild(xml);

            // set up a transformer
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");

            // create string from xml tree
            StreamResult result = new StreamResult(writer);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
            writer.flush();
            writer.close();
        } catch (ParserConfigurationException e) {
            System.out.println("Configuration error: Unable to create document builder");
            return false;
        } catch (TransformerConfigurationException e) {
            System.out.println("Configuration error: Unable to create transformer");
            return false;
        } catch (TransformerException e) {
            System.out.println("Error while transforming XML document");
            return false;
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return false;
        } catch (IOException e) {
            System.out.println("IO error");
            return false;
        }
        return true;
    }

    private Element GetXml(Document document) {
        if (isUnary) {
            Element elem = document.createElement("Value");
            elem.appendChild(document.createTextNode(String
                    .valueOf(leftValue)));
            return elem;
        } else if (left != null
                && right != null) {
            Element elem = document.createElement(XmlGetNodeName());
            elem.appendChild(left.GetXml(document));
            elem.appendChild(right.GetXml(document));
            return elem;
        } else if (left == null
                && right == null) {
            Element elem = document.createElement(XmlGetNodeName());
            elem.appendChild(new Expression(leftValue).GetXml(document));
            elem.appendChild(new Expression(rightValue).GetXml(document));
            return elem;
        } else {
            // We should never get here
            System.out.println("Invalid state!");
            return null;
        }
    }

    private String XmlGetNodeName() {
        switch (op) {
        case "+":
            return "Add";
        case "-":
            return "Subtract";
        case "*":
            return "Multiply";
        case "/":
            return "Divide";
        }
        // We should never get here
        System.out.println("Invalid state!");
        return null;
    }
}
