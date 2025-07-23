import menu.MenuPrincipal

@main
def main(): Unit = {
  DB.connection.createStatement();

  MenuPrincipal.afficherMenuPrincipal();
}

