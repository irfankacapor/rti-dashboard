import { useTranslations } from 'next-intl';
import { Container, Typography, Box, Button } from '@mui/material';

export default function Home() {
  const t = useTranslations('Index');

  return (
    <Container maxWidth="lg">
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          textAlign: 'center',
          gap: 4,
        }}
      >
        <Typography variant="h2" component="h1" gutterBottom>
          {t('title')}
        </Typography>
        
        <Typography variant="h5" component="h2" color="text.secondary" paragraph>
          {t('description')}
        </Typography>

        <Box sx={{ mt: 4 }}>
          <Button variant="contained" size="large">
            Get Started
          </Button>
        </Box>
      </Box>
    </Container>
  );
}
