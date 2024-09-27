
import br.com.interfaces.model.IMusica;
import br.com.interfaces.model.IPlaylist;
import br.com.interfaces.model.IUsuario;
import br.com.interfaces.repository.IMusicaRepository;
import br.com.interfaces.repository.IUsuarioRepository;
import br.com.interfaces.services.IRecomendacaoService;
import br.com.interfaces.services.IReproducaoService;
import br.com.model.Usuario;
import br.com.repositories.MusicaRepository;
import br.com.repositories.UsuarioRepository;
import br.com.services.PlayListService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

public class MockTestes {
    private PlayListService playListService;
    private IUsuarioRepository usuarioRepository;
    private IUsuario usuario;
    private Optional<IPlaylist> pl;
    
    @Mock
    private IReproducaoService mockReproducao;

    @Mock
    private IRecomendacaoService mockRecomendacao;
    
    @BeforeEach
    public void setUp() throws Exception{
        MockitoAnnotations.openMocks(this);
        
        playListService = new PlayListService(mockRecomendacao);
        usuario = new Usuario("Jao", "jao@email", Boolean.TRUE, Boolean.TRUE);
        
        usuarioRepository = UsuarioRepository.getUsuarioRepository();
        usuarioRepository.inserir(usuario);
        
        // Definindo uma playlist valida
        pl = playListService.criarPlayList("NaoSei", usuario);
        var musicas = MusicaRepository.getMusicaRepository().getMusicas("BlackPink");
        for(IMusica music : musicas.get() ){
            pl.get().adicionarMusica(music);
        }
    }
    
    @Test
    public void criarPlayListTeste(){
        Optional<IPlaylist> playList = playListService.criarPlayList("Qualquer", usuario);
        
        assertEquals( "Qualquer", playList.get().getNome() );
        assertEquals( "Jao", playList.get().getCriador().getNome() );
        assertEquals( "jao@email", playList.get().getCriador().getEmail());
    }
    
    @Test
    public void criarPlayListTesteFalha1(){
        Optional<IPlaylist> playList = playListService.criarPlayList("", usuario);
        
        assertEquals( Optional.empty(), playList);
    }
    
    @Test
    public void criarPlayListTesteFalha2(){
        usuario = new Usuario("Jao", "JaoJaojao@email", Boolean.TRUE, Boolean.FALSE);
        
        Optional<IPlaylist> playList = playListService.criarPlayList("Qualquer", usuario);
        
        assertEquals( Optional.empty(), playList);
    }
    
    @Test
    // A playlist é compartilhada com o usuário especificado.
    public void compartilharPlayListTeste(){
        Usuario user = new Usuario("Eu", "eu@email", Boolean.TRUE, Boolean.FALSE);
        
        usuarioRepository.inserir(user);
        
        var retorno = playListService.compartilharPlayList(pl.get(), user);
        assertEquals(Boolean.TRUE, pl.get().getUsuarios().contains(user));
        assertEquals(0, retorno);
    }
    
    @Test
    // Retorna um valor indicando falha se o usuário não existir no sistema.
    public void compartilharPlayListTesteFalha1(){
        Usuario user = new Usuario("TesteFalha1", "eu@email", Boolean.TRUE, Boolean.FALSE);
        
        var retorno = playListService.compartilharPlayList(pl.get(), user);
        
        assertEquals(Boolean.FALSE, pl.get().getUsuarios().contains(user));
        assertEquals(-1, retorno);
    }
    
    @Test
    // Retorna um valor indicando falha se a playlist for privada e o compartilhamento não for permitido
    public void compartilharPlayListTesteFalha2(){
        Usuario user = new Usuario("Eu", "eu@email", Boolean.TRUE, Boolean.FALSE);
        usuarioRepository.inserir(user);
        
        pl.get().setCompartilhavel(false);
        pl.get().setPublica(false);
        
        var retorno = playListService.compartilharPlayList(pl.get(), user);
        
        assertEquals(Boolean.FALSE, pl.get().getUsuarios().contains(user));
        assertEquals(-2, retorno);
    }
    
    @Test
    // Adiciona o usuário como colaborador da playlist com sucesso.
    public void convidarColaboradorTeste(){
        Usuario user = new Usuario("Eu", "eu@email", Boolean.TRUE, Boolean.FALSE);
        
        var tamanho = pl.get().getColaboradores().size();
        var retorno = playListService.convidarColaborador(pl.get(), user);
        
        
        assertEquals(tamanho+1, pl.get().getColaboradores().size());
        assertEquals(Boolean.TRUE, pl.get().getColaboradores().contains(user));
        assertEquals(0, retorno);
    }
    
    @Test
    // Retorna um valor indicando que o colaborador já faz parte da playlist.
    public void convidarColaboradorTesteFalha1(){
        Usuario user = new Usuario("Eu", "eu@email", Boolean.TRUE, Boolean.FALSE);
        
        playListService.convidarColaborador(pl.get(), user);
        
        var retorno = playListService.convidarColaborador(pl.get(), user);
        var tamanho = pl.get().getColaboradores().size();
        
        assertEquals(tamanho, pl.get().getColaboradores().size());
        assertEquals(Boolean.TRUE, pl.get().getColaboradores().contains(user));
        assertEquals(-1, retorno);
    }
    
    @Test
    // Retorna um valor indicando falha se a playlist atingiu o limite máximo de colaboradores
    public void convidarColaboradorTesteFalha2(){
        Usuario user1 = new Usuario("Eu1", "eu1@email", Boolean.TRUE, Boolean.FALSE);
        Usuario user2 = new Usuario("Eu2", "eu2@email", Boolean.TRUE, Boolean.FALSE);
        Usuario user3 = new Usuario("Eu3", "eu3@email", Boolean.TRUE, Boolean.FALSE);
        Usuario user4 = new Usuario("Eu4", "eu4@email", Boolean.TRUE, Boolean.FALSE);
        Usuario user5 = new Usuario("Eu5", "eu5@email", Boolean.TRUE, Boolean.FALSE);
        
        playListService.convidarColaborador(pl.get(), user1);
        playListService.convidarColaborador(pl.get(), user2);
        playListService.convidarColaborador(pl.get(), user3);
        playListService.convidarColaborador(pl.get(), user4);
        
        var retorno = playListService.convidarColaborador(pl.get(), user5);
        var tamanho = pl.get().getColaboradores().size();
        
        assertEquals(tamanho, pl.get().getColaboradores().size());
        assertEquals(Boolean.FALSE, pl.get().getColaboradores().contains(user5));
        assertEquals(-2, retorno);
    }
    
    @Test
    // Retorna uma lista de músicas recomendadas para adicionar à playlist.
    public void recomendarMusicasParaPlayListTeste(){
        IMusicaRepository musicaRepository = MusicaRepository.getMusicaRepository();
        List<IMusica> musicasRecomendadas = new ArrayList<>();
        musicasRecomendadas.add(musicaRepository.findByTitulo("What is Love").get());
        musicasRecomendadas.add(musicaRepository.findByTitulo("Gangnam Style").get());

        when(mockRecomendacao.recomendarMusicasParaPlayList(pl.get())).thenReturn(musicasRecomendadas);
        
        List<IMusica> resultado = playListService.recomendarMusicasParaPlayList(pl.get());
        
        verify(mockRecomendacao, times(1)).recomendarMusicasParaPlayList(pl.get());
        assertEquals(Boolean.FALSE , resultado.isEmpty());
        assertTrue(resultado.contains(musicaRepository.findByTitulo("What is Love").get()));
        assertTrue(resultado.contains(musicaRepository.findByTitulo("Gangnam Style").get()));
    }
    
    @Test
    // Retorna uma lista vazia se não houver músicas disponíveis para recomendação.
    public void recomendarMusicasParaPlayListTesteFalha1(){
        List<IMusica> musicasRecomendadas = new ArrayList<>();
        
        when(mockRecomendacao.recomendarMusicasParaPlayList(pl.get())).thenReturn(musicasRecomendadas);
        
        List<IMusica> resultado = playListService.recomendarMusicasParaPlayList(pl.get());
        
        verify(mockRecomendacao, times(1)).recomendarMusicasParaPlayList(pl.get());
        assertEquals(Boolean.TRUE , resultado.isEmpty());
    }
    
    @Test
    // Retorna uma lista vazia se o RecomendacaoService não estiver disponível.
    public void recomendarMusicasParaPlayListTesteFalha2() {
        // Simulando que o RecomendacaoService está indisponivel
        PlayListService service = new PlayListService(null);
        
        // Chamando o método que vai utilizar o serviço de recomendação
        List<IMusica> resultado = service.recomendarMusicasParaPlayList(pl.get());

        // Verificando se o resultado é uma lista vazia, já que o serviço está indisponível (retornou null)
        assertTrue(resultado.isEmpty());
    }

    @Test
    // Inicia a reprodução da playlist automaticamente.
    public void iniciarReproducaoPlayList(){
        doNothing().when(mockReproducao).reproduzirPlayList(eq(pl.get()), eq(usuario), any());
        
        var retorno = playListService.iniciarReproducaoPlayList(pl.get(), usuario, mockReproducao);
        
        assertEquals(0, retorno);
        verify(mockReproducao, times(1)).reproduzirPlayList(eq(pl.get()), eq(usuario), any());
    }
    
    @Test
    // Retorna um valor indicando falha se o usuário não tiver permissão para reproduzir músicas.
    public void iniciarReproducaoPlayListFalha1(){
        IUsuario us = new Usuario("Meu nome", "email@email", Boolean.FALSE, Boolean.TRUE);
        
        var playListVazia = playListService.criarPlayList("Vazia", us);
        
        doNothing().when(mockReproducao).reproduzirPlayList(eq(playListVazia.get()), eq(us), any());
        
        var retorno = playListService.iniciarReproducaoPlayList(playListVazia.get(), us, mockReproducao);
        
        assertEquals(-1, retorno);
        verify(mockReproducao, never()).reproduzirPlayList(eq(playListVazia.get()), eq(us), any());
    }
    
    @Test
    // Retorna um valor indicando falha se a playlist estiver vazia
    public void iniciarReproducaoPlayListFalha2(){
        var playListVazia = playListService.criarPlayList("Vazia", usuario);
        
        doNothing().when(mockReproducao).reproduzirPlayList(eq(playListVazia.get()), eq(usuario), any());
        
        var retorno = playListService.iniciarReproducaoPlayList(playListVazia.get(), usuario, mockReproducao);
        
        assertEquals(-2, retorno);
        verify(mockReproducao, never()).reproduzirPlayList(eq(playListVazia.get()), eq(usuario), any());
    }
}
